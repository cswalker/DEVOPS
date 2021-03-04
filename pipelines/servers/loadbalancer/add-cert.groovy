pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod', 'shared'], description: 'Provide the environment type')
        string(name: 'env_name', defaultValue: '', description: 'Provide the name of this environment')
        string(name: 'server_name', defaultValue: 'loadbalancer', description: 'Provide the name of the loadbalancer to be modified')
        string(name: 'domain', defaultValue: '', description: "Provide the domain name of the certificate")
        string(name: 'name', defaultValue: '', description: "Provide the name of the certificate (alphanumeric and _ only)")
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_type} ${env_name} ${server_name} - ${name} ${domain}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    validators.environmentName(params.env_name)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    instance.failOnNotExists(env_name, server_name, env_type)
                }
            }
        }
        stage('Add Certificate') {
            steps {
                lock(resource: 'certbot') {
                    script {
                        writeFile file: 'inventory.yaml', text: """[${env_type}]
                        localhost ansible_python_interpreter=/usr/bin/python3"""

                        sh """
                            ansible-playbook -i inventory.yaml \
                                -e arg_env_type=${env_type} \
                                -e arg_env_name=${env_name} \
                                -e arg_server_name=${server_name} \
                                -e arg_domain=${domain} \
                                -e arg_name=${name} \
                                provisioners/loadbalancer/ansible/add-cert.yaml
                        """
                    }
                }
            }
        }
    }
}
