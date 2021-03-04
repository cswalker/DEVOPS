pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'ip_address', defaultValue: '', description: 'Provide the IP address of the server to add to a backend set')
        choice(name: 'action', choices: ['add', 'remove'], description: 'Choose the action to take on the backend')

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
                    instance.failOnNotExists(env_name, "loadbalancer", env_type)
                }
            }
        }
        stage('Change Load Balancer Backend') {
            steps {
                script {
                    println "${action} ${ip_address} to load balancer backend set"

                    loadbalancer_info = instance.info(env_name, "loadbalancer", env_type)

                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost"""
                    sh """
                        ansible-playbook \
                            -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_ip_address=${ip_address} \
                            -e arg_loadbalancer_id=${loadbalancer_info.loadbalancer.id} \
                            provisioners/loadbalancer/ansible/${action}.yaml
                    """
                }
            }
        }
    }
}
