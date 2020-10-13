pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'shared', 'qa', 'uat'], description: 'The environment the server is created in')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
        choice(name: 'role', choices: ['primary', 'secondary'], description: 'Should the dns be Primary or Secondary')
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
        stage('Create Server') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
                script {
                    writeFile file: 'dns-inventory.yaml', text: """[${env_type}_${role}]
                    localhost"""

                    sh """
                        ansible-playbook -i dns-inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_instance_ad=sSxD:${instance_ad} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/dns/ansible/create-oci-instance.yaml
                    """
                }
            }
        }
    }
}
