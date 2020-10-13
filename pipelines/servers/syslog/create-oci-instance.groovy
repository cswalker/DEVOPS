pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: 'syslog', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
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
                    instance.failOnExists(env_name, server_name, env_type)
                }
            }
        }
        stage('Create Server') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_instance_ad=sSxD:${instance_ad} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/syslog/ansible/create-oci-instance.yaml
                    """
                }
            }
        }
    }
}
