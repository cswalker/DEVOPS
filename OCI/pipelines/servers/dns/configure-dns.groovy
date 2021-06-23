pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        choice(name: 'instance_role', choices: ['primary', 'secondary'], description: 'Should the dns be Primary or Secondary')

    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.instance_role)
                    env_name = 'dns'
                    if (params.instance_role == 'primary') {
                        server_name = '01'
                    } else {
                        server_name = '02'
                    }

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
        stage('Configure DNS') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    ${params.env_type}_${params.instance_role} ansible_host=${server_info.instance.private_ip}"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${params.env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_instance_role=${params.instance_role} \
                            provisioners/dns/ansible/configure-dns.yaml
                    """
                }
            }
        }
    }
}
