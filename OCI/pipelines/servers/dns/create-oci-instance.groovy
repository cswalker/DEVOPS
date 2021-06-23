pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
        string(name: 'instance_shape', defaultValue: 'VM.Standard.E4.Flex', description: 'Compute shape the instance will be built on')
        string(name: 'instance_cpu', defaultValue: '2', description: 'The number of CPUs to provision when using a flex shape')
        string(name: 'instance_memory', defaultValue: '8', description: 'The amount of memory in GBs to provision when using a flex shape')
        choice(name: 'instance_role', choices: ['primary', 'secondary'], description: 'The Role of the Domain Controller Instance')
        string(name: 'base_image', defaultValue: 'stable', description: 'Which base image to use (stable, latest, or specific version number). Use 0 for bare ol7 image')
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

                    shared.loadInstanceInfo().failOnExists(env_name, server_name, params.env_type)
                    image_info = shared.loadImageInfo().info("dns", params.base_image)
                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_type} ${env_name} ${server_name}"
                    println "Base Image OCID: ${image_info.id}"
                }
            }
        }
        stage('Create a DNS Server') {
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
                            -e arg_instance_shape=${params.instance_shape} \
                            -e arg_instance_cpu=${params.instance_cpu} \
                            -e arg_instance_memory=${params.instance_memory} \
                            -e arg_instance_role=${params.instance_role} \
                            -e arg_jenkins_user=${build_user} \
                            -e arg_instance_image=${image_info.id} \
                            provisioners/dns/ansible/create-oci-instance.yaml
                    """
                }
            }
        }
    }
}
