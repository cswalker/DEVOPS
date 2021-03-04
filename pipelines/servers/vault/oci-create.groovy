pipeline {
    agent any

    environment {
        env_type = 'shared'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        string(name: 'server_name', defaultValue: 'vault01', description: 'Provide a URL safe unique name for this server')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    instance.failOnExists('vaults', server_name, env_type)
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
                            -e arg_server_name=${server_name} \
                            -e arg_instance_ad=sSxD:${instance_ad} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/vault/ansible/oci-create.yaml
                    """
                }
            }
        }
    }
}
