pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod', 'shared'], description: 'Provide the environment type')
        string(name: 'env_name', defaultValue: '', description: 'Provide the name of this environment')
        string(name: 'server_name', defaultValue: 'loadbalancer', description: 'Provide the name of the loadbalancer to be modified')
        string(name: 'backendset_name', defaultValue: 'loadbalancer', description: 'Provide a name for the new backend set')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)
                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_type} ${env_name} ${server_name} - ${backendset_name}"
                }
            }
        }
        stage('Remove Backend Set') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_backendset_name=${backendset_name} \
                            provisioners/loadbalancer/ansible/backendset-remove.yaml
                    """
                }
            }
        }
    }
}
