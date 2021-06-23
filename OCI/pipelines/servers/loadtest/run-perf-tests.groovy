pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: 'loadtest', description: 'Provide a URL safe unique name for this server')
        string(name: 'jmeter_app_server_name', defaultValue: '', description: 'Target app server name where JMeter scripts will run against (e.g. perf02-ibex). Leave blank to use script defaults.')
        choice(name: 'jmeter_maven_profile', choices: ['all', 'core', 'encore'], description: 'Maven Profile to run tests with.')
        string(name: 'jmeter_thread_count', defaultValue: '25', description: 'Number of threads (users)')
        string(name: 'jmeter_ramp_up_in_seconds', defaultValue: '120', description: 'Ramp-up period (seconds)')
        string(name: 'jmeter_loop_count', defaultValue:  '100', description: 'Loop count')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)
                    currentBuild.displayName = "#${BUILD_NUMBER} ${jmeter_app_server_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    validators.environmentName(params.env_name)
                }
            }
        }
        stage('Running Test Plans') {
            steps {
                script {
                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(env_name, server_name, env_type)
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_jmeter_app_server_name=${jmeter_app_server_name} \
                            -e arg_jmeter_maven_profile=${jmeter_maven_profile} \
                            -e arg_jmeter_thread_count=${jmeter_thread_count} \
                            -e arg_jmeter_ramp_up_in_seconds=${jmeter_ramp_up_in_seconds} \
                            -e arg_jmeter_loop_count=${jmeter_loop_count} \
                            provisioners/loadtest/ansible/run-perf-tests.yaml
                    """
                }
            }
        }
    }
    post {
        always {
            echo 'Publishing reports'
            script{
                instance = load pwd() + "/pipelines/shared/instance.groovy"
                server_info = instance.info(env_name, server_name, env_type)
                writeFile file: 'inventory.yaml', text: """[${env_type}]
                ${server_info.instance.private_ip}"""
                sh """
                    ansible-playbook -i inventory.yaml \
                    provisioners/loadtest/ansible/get-perf-reports.yaml
                    rm -rf ${env.WORKSPACE}/perf_reports
                    mkdir -p ${env.WORKSPACE}/perf_reports
                    cp /tmp/${server_info.instance.private_ip}/tmp/perf_reports.zip ${env.WORKSPACE}/perf_reports/perf_reports.zip
                    unzip -q /tmp/${server_info.instance.private_ip}/tmp/perf_reports.zip -d ${env.WORKSPACE}/perf_reports/
                """
                archiveArtifacts artifacts: "**/perf_reports/perf_reports.zip"
                publishHTML (target : [allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'perf_reports',
                    reportFiles: '*/index.html',
                    reportName: 'Performance reports',
                    reportTitles: 'Performance'])
            }
        }
    }
}
