pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod', 'shared'], description: 'Provide the environment type')
        string(name: 'env_name', defaultValue: '', description: 'Provide the name of this environment')
        string(name: 'server_name', defaultValue: 'loadbalancer', description: 'Provide the name of the loadbalancer to be modified')
        string(name: 'backendset_name', defaultValue: '', description: 'Provide the name of the backend set to update')
        string(name: 'ip_address', defaultValue: '', description: 'Provide the ip_address of the backend')
        string(name: 'port', defaultValue: '80', description: 'Provide the port of the backend')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)
                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_type} ${env_name} ${server_name} - ${backendset_name} ${ip_address}:${port}"
                }
            }
        }
        stage('Remove Backend') {
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
                            -e arg_ip_address=${ip_address} \
                            -e arg_port=${port} \
                            provisioners/loadbalancer/ansible/backend-remove.yaml
                    """
                }
            }
        }
    }
}
