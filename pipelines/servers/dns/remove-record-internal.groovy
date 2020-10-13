pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'shared', 'qa', 'uat'], description: 'The environment the server is created in')
        string(name: 'record', defaultValue: '', description: 'Provide a URL safe unique name for this record')
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
                            -l ${env_type}_primary \
                            -e arg_record='${record}' \
                            provisioners/dns/ansible/remove-dns-internal.yaml
                    """
                }
            }
        }
    }
}
