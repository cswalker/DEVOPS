pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['init', 'run'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'shared',], description: 'The environment the server is created in')
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
        stage('Configure DNS') {
            steps {
                script {
                    sh """
                        ansible-playbook -i provisioners/dns/ansible/inventory.yaml \
                            -l ${env_type}_${role} \
                            provisioners/dns/ansible/configure-dns.yaml
                    """
                }
            }
        }
    }
}
