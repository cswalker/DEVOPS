pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
        choice(name: 'instance_role', choices: ['primary', 'secondary'], description: 'The Role of the Domain Controller Instance')
   }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.instance_role)
                    env_name = 'dns'
                    if (params.instance_role == 'primary') {
                        server_name = '01'
                    } else {
                        server_name = '02'
                    }

                    shared.loadInstanceInfo().failOnNotExists(env_name, server_name, params.env_type)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_type} ${env_name} ${server_name}"
                }
            }
        }
        stage('Create a DNS Volume') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    ${params.env_type}_${params.instance_role} ansible_host=localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${params.env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_instance_ad=sSxD:${params.instance_ad} \
                            -e arg_instance_role=${params.instance_role} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/dns/ansible/create-oci-volume.yaml
                    """
                }
            }
        }
    }
}
