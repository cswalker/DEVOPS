pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
        choice(name: 'instance_role', choices: ['primary', 'secondary'], description: 'The Role of the Domain Controller Instance')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    env_name = 'dns'
                    if (params.instance_role == 'primary') {
                        server_name = '01'
                    } else {
                        server_name = '02'
                    }

                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(env_name)


                    shared.loadInstanceInfo().failOnNotExists(env_name, server_name, params.env_type)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_type} ${env_name} ${server_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    server_info = shared.loadInstanceInfo().info(env_name, server_name, params.env_type)
                }
            }
        }
        stage('Terminate Server') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    ${server_info.instance.private_ip}"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${params.env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            provisioners/dns/ansible/destroy-oci-instance.yaml
                    """
                }
            }
        }
    }
}
