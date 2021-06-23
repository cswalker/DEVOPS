pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod', 'shared'], description: 'Provide the environment type')
        string(name: 'env_name', defaultValue: '', description: 'Provide the name of this environment')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_type} ${env_name}"
                }
            }
        }
        stage('Get Certificates') {
            steps {
                lock(resource: 'certbot') {
                    script {
                        def instance = load pwd() + "/pipelines/shared/instance.groovy"
                        def arg_environment_config_file = ""

                        if (env_type == 'shared' && env_name == 'static') {
                            writeFile file: 'inventory.yaml', text: """[static]
                                localhost ansible_python_interpreter=/usr/bin/python3"""
                        } else {
                            writeFile file: 'inventory.yaml', text: """[${env_type}]
                                localhost ansible_python_interpreter=/usr/bin/python3"""
                            arg_environment_config_file = "-e arg_configuration_file=${instance.getConfiguration(env_name, env_type)}"
                        }


                        sh """
                            ansible-playbook -i inventory.yaml \
                                -e arg_env_type=${env_type} \
                                -e arg_env_name=${env_name} \
                                ${arg_environment_config_file} \
                                provisioners/loadbalancer/ansible/get-certs.yaml
                        """
                    }
                }
            }
        }
    }
}
