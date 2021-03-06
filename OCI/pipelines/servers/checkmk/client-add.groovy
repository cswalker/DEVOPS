pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'prod', 'uat', 'shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'checkmk_server_url', defaultValue: '', description: 'URL of Check_MK server dashboard')
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
                    println "${server_info.instance.private_ip}"
                }
            }
        }
        stage('Add Host') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e "arg_checkmk_server_url=${checkmk_server_url}" \
                            provisioners/checkmk/ansible/checkmk-client-add.yaml
                    """
                }
            }
        }
    }
}
