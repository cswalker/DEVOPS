pipeline {
    agent any

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['shared'], description: 'The environment the server is created in')
        choice(name: 'env_name', choices: ['static'], description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
        string(name: 'profile', defaultValue: '', description: 'Provide the profile name of the environment for which we are setting up assets')
    }

    options {
        disableConcurrentBuilds() // multiple runs in parallel might try to delete the same file and fail
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

                    println "Static Server IP: ${server_info.instance.private_ip}"

                    currentBuild.displayName = "#${BUILD_NUMBER} ${profile}"
                }
            }
        }
        stage('Delete old assets') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                ${server_info.instance.private_ip}"""
                    sh """
                    ansible-playbook -i inventory.yaml \
                        -e arg_env_name=${env_name} \
                        -e arg_env_type=${env_type} \
                        -e arg_server_name=${server_name} \
                        -e arg_profile=${profile} \
                        provisioners/static/ansible/assets-cleanup.yaml
                """
                }
            }
        }
    }
}
