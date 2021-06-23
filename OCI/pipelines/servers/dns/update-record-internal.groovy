pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        string(name: 'record', defaultValue: '', description: 'Provide a URL safe unique name for this record')
        string(name: 'ip_address', defaultValue: "", description: "IP Address for the record to point to")
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    env_name = 'dns'
                    instance_role = 'primary'
                    server_name = '01'

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_type} ${env_name} ${server_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    server_info = shared.loadInstanceInfo().info(env_name, server_name, params.env_type)
                    println "${server_info.instance.private_ip}"
                }
            }
        }


        stage('Configure Internal DNS Record') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    ${params.env_type}_${instance_role} ansible_host=${server_info.instance.private_ip}"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${instance_role} \
                            -e arg_record='${record}' \
                            -e arg_instance_private_ip=${ip_address} \
                            -e arg_instance_role=${instance_role} \
                            provisioners/dns/ansible/update-dns-internal.yaml
                    """
                }
            }
        }
    }
}
