pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: 'pgbouncer', description: 'Provide a URL safe unique name for this server')
        string(name: 'managed_block_name', defaultValue: '', description: 'Unique block name for configuration management')
        text(name: 'hba_conf_entry', defaultValue: '', description: 'The String to add to the HBA Configuration file')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name} ${server_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    validators.environmentName(params.env_name)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(env_name, server_name, env_type)
                    println "PGBouncer Server IP: ${server_info.instance.private_ip}"

                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
            }
        }
        stage('Gather Facts') {
            steps {
                script {
                    println "PGBouncer Server IP: ${server_info.instance.private_ip}"
                }
            }
        }
        stage('Configure PGBouncer HBA Conf') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e "arg_hba_conf_entry='${hba_conf_entry}'" \
                            -e arg_block_name=${managed_block_name} \
                            provisioners/pgbouncer/ansible/update-configuration.yaml
                    """
                }
            }
        }
    }
}
