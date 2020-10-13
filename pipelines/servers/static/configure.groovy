pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['shared'], description: 'The environment the server is created in')
        choice(name: 'env_name', choices: ['static'], description: 'Provide a URL safe unique name for this environment')
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

                    println "${server_info.instance.private_ip}"

                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
            }
        }
        stage('Base Install') {
            steps {
                script {
                    writeFile file: 'cdn-static-inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i cdn-static-inventory.yaml \
                            -e arg_env_name=${env_name} \
                            -e arg_env_type=${env_type} \
                            -e arg_server_name=${server_name} \
                            -e arg_public_ip=${server_info.instance.public_ip} \
                            provisioners/static/ansible/configure.yaml
                    """
                }
            }
        }
    }
}
