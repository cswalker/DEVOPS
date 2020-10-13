pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'shared', 'qa', 'uat'], description: 'The environment type the server is created in')
        string(name: 'record_name', defaultValue: '', description: 'Provide a URL safe unique name for this record')
        string(name: 'record_value', defaultValue: '', description: 'Provide a value for the record to point to')
        choice(name: 'record_type', choices: ['A', 'CNAME'], description: 'Provide the Record Type for this record')
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

        stage('Configure DNSMadeEasy DNS') {
            steps {
                script {
                    sh """
                        ansible-playbook \
                            -e arg_record_name='${record_name}.${env_type}.oci' \
                            -e arg_record_value='${record_value}' \
                            -e arg_record_type='${record_type}' \
                            provisioners/dns/ansible/update-dns-dnsmadeeasy.yaml
                    """
                }
            }
        }
    }
}
