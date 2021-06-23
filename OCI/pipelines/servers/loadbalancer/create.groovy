pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod', 'shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: 'loadbalancer', description: 'Provide a URL safe unique name for this environment')
    }
    options {
        disableConcurrentBuilds()
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

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_type} ${env_name} ${server_name}"
                }
            }
        }
        stage('Create Load Balancer') {
            steps {
                script {
                    def build = load pwd() + "/pipelines/shared/build.groovy"
                    def instance = load pwd() + "/pipelines/shared/instance.groovy"
                    def config = instance.readConfiguration(env_type, env_name)

                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_profile=${config.profile} \
                            -e arg_jenkins_user=${build.getBuildUser()} \
                            -e arg_configuration_file=${instance.getConfiguration(env_name, env_type)} \
                            provisioners/loadbalancer/ansible/create.yaml
                    """
                }
            }
        }
    }
}
