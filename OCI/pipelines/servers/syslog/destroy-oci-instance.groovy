pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: 'syslog', description: 'Provide the environment name to be destroyed')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
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
        stage('Validate Input') {
            steps {
                script {
                    validators.environmentName(params.env_name)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(env_name, server_name, env_type)
                }
            }
        }
        stage('Terminate Server') {
            steps {
                script {
                    sh """
                        ansible-playbook \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            provisioners/syslog/ansible/destroy-oci-instance.yaml
                    """
                }
            }
        }
    }
}
