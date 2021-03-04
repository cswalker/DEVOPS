pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['init', 'run'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'prod', 'uat', 'shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name the server is created in')
        string(name: 'cmkhost_name', defaultValue: '', description: 'Hostname which needs to be deleted from Check_MK dashboard')
        string(name: 'checkmk_server_url', defaultValue: '', description: 'URL of Check_MK server dashboard') 
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name} ${cmkhost_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    validators.environmentName(params.env_name)  
                }
            }
        }
        stage('Delete host') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""
                    
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_cmkhost_name=${cmkhost_name} \
                            -e arg_checkmk_server_url=${checkmk_server_url} \
                            provisioners/checkmk/ansible/delete-checkmk-host.yaml
                    """
                }
            }
        }
    }
}
