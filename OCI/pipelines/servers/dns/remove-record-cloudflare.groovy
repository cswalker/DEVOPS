pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        string(name: 'record_name', defaultValue: '', description: 'Provide a URL safe unique name for this record')
        string(name: 'record_value', defaultValue: '', description: "Provide a Value for the record to point to")
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    env_type = 'cloudflare'
                }
            }
        }

        stage('Configure Cloudflare DNS') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_record_name='${record_name}' \
                            -e arg_record_value='${record_value}' \
                            provisioners/dns/ansible/remove-dns-cloudflare.yaml
                    """
                }
            }
        }
    }
}
