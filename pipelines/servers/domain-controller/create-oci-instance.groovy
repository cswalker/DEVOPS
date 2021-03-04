pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: 'dc01', description: 'Provide a URL safe unique name for this server')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
        string(name: 'instance_shape', defaultValue: 'VM.Standard.E3.Flex', description: 'Compute shape the instance will be built on')
        string(name: 'instance_cpu', defaultValue: '2', description: 'The number of CPUs to provision when using a flex shape')
        string(name: 'instance_memory', defaultValue: '8', description: 'The amount of memory in GBs to provision when using a flex shape')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_name} ${params.server_name}"
                }
            }
        }
        stage('Validate Input') {
            steps {
                script {
                    shared.loadValidators().environmentName(params.env_name)
                    shared.loadInstanceInfo().failOnExists(params.env_name, params.server_name, params.env_type)
                }
            }
        }
        stage('Create a Domain Controller Server') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
                script {
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${params.env_type} \
                            -e arg_env_name=${params.env_name} \
                            -e arg_server_name=${params.server_name} \
                            -e arg_instance_ad=sSxD:${params.instance_ad} \
                            -e arg_instance_shape=${params.instance_shape} \
                            -e arg_instance_cpu=${params.instance_cpu} \
                            -e arg_instance_memory=${params.instance_memory} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/domain-controller/ansible/create-oci-instance.yaml
                    """
                }
            }
        }
    }
}
