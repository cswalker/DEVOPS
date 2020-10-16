pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name to be destroyed')
        string(name: 'server_name', defaultValue: '', description: 'Provide a URL safe unique name for this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_exists = fileExists(instance.file(env_name, server_name, env_type))
                }
            }
        }
        stage('Leave Active Directory') {
            when {
                equals expected: true, actual: server_exists
            }
            steps {
                script {
                    server_info = instance.info(env_name, server_name, env_type)
                    
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            provisioners/common/ansible/realmd-leave.yaml
                    """
                }
            }
        }
    }
}
