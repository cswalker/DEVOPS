pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['uat', 'shared'], description: 'Provide the environment type')
        string(name: 'env_name', defaultValue: '', description: 'Provide the name of this environment')
        string(name: 'server_name', defaultValue: 'loadbalancer', description: 'Provide the name of the loadbalancer to be modified')
        string(name: 'url_name', defaultValue: '', description: """Provide the name of the URL. 
            This will be used to name the secrets, certificate, hostname, and listener""")
        string(name: 'url_domain', defaultValue: '', description: """Provide the domain for the listener. 
            The certificate will be determined automatically if under the switchfly.com domain, 
            otherwise it must be manually added to the vault prior to running this job.""")
        string(name: 'backendset_name', defaultValue: 'backend_servers_set', description: 'Provide the name of the backend set for the listener')

    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)
                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_type} ${env_name} ${server_name} - ${url_name}"
                }
            }
        }
        stage('Update Backend') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_url_name=${url_name} \
                            -e arg_url_domain=${url_domain} \
                            -e arg_backendset_name=${backendset_name} \
                            provisioners/loadbalancer/ansible/url-update.yaml
                    """
                }
            }
        }
    }
}
