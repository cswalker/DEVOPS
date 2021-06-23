pipeline {
    agent any

    environment {
        env_type = 'shared'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        string(name: 'server_name', defaultValue: 'vault01', description: 'Provide a URL safe unique name for this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info("vaults", server_name, env_type)
                    println "${server_name} IP: ${server_info.instance.private_ip}"
                }
            }
        }
        stage('Configure DNS') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_server_name=${server_name} \
                            provisioners/vault/ansible/initialize.yaml
                    """
                }
            }
        }
    }
}
