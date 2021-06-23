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
                }
            }
        }
        stage('Create Server') {
            steps {
                script {
                    sh """
                        ansible-playbook \
                            -e arg_env_type=${env_type} \
                            -e arg_server_name=${server_name} \
                            provisioners/vault/ansible/oci-destroy.yaml
                    """
                }
            }
        }
    }
}
