pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['corp'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: 'dc', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.env_name)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_name} ${params.server_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    server_info = shared.loadInstanceInfo().info(params.env_name, params.server_name, params.env_type)
                    println "${server_info.instance.private_ip}"
                }
            }
        }
        stage('Promote Member Server to Domain Controller') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    domain-controller ansible_host=${server_info.instance.private_ip} ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${params.env_type} \
                            -e arg_env_name=${params.env_name} \
                            -e arg_server_name=${params.server_name} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/domain-controller/ansible/dc-promote.yaml
                    """
                }
            }
        }
    }
}
