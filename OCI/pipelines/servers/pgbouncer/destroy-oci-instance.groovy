pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name to be destroyed')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    validators.environmentName(params.env_name)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(env_name, "pgbouncer", env_type)
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
                            provisioners/pgbouncer/ansible/destroy-oci-instance.yaml
                    """
                }
            }
        }
    }
}
