pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['corp'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: 'dc', description: 'Provide the environment name to be destroyed')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.env_name)
                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(params.env_name, params.server_name, params.env_type)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_name} ${params.server_name}"
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
                            -e arg_env_name=${params.env_name} \
                            -e arg_server_name=${params.server_name} \
                            provisioners/domain-controller/ansible/destroy-oci-instance.yaml
                    """
                }
            }
        }
    }
}
