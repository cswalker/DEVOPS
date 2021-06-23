pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name (ex. v211test-v1)')
        string(name: 'server_name', defaultValue: '', description: 'Provide a URL safe unique name for this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.env_name)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_name} ${params.server_name}"
                }
            }
        }
        stage('Configure Active Directory') {
            steps {
                script {
                    server_info = shared.loadInstanceInfo().info(params.env_name, params.server_name, params.env_type)
                    println "${server_info.instance.private_ip}"

                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            provisioners/common/ansible/realmd-install.yaml
                    """
                }
            }
        }
    }
}
