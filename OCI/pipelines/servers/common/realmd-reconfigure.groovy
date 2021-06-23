pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name (ex. v211test-v1)')
        string(name: 'server_name', defaultValue: '', description: 'Provide a URL safe unique name for this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_type} ${params.env_name} ${params.server_name}"
                }
            }
        }
        stage('Leave Active Directory') {
            steps {
                script {
                    build job: 'common-realmd-leave',
                        parameters: [
                            string(name: 'job', value: params.job),
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'env_name', value: params.env_name),
                            string(name: 'server_name', value: params.server_name),
                        ]
                }
            }
        }
        stage('ReConfigure Active Directory') {
            steps {
                script {
                    build job: 'common-realmd-configure',
                        parameters: [
                            string(name: 'job', value: params.job),
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'env_name', value: params.env_name),
                            string(name: 'server_name', value: params.server_name),
                        ]
                }
            }
        }
    }
}
