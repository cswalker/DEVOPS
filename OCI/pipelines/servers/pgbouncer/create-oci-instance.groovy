pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availability domain the instance will be created in')
        choice(name: 'instance_shape', choices: ['VM.Standard.E3.Flex', 'VM.Standard2.1', 'VM.Standard.E2.1', 'VM.Standard.E2.2', 'VM.Standard.E2.4', 'VM.Standard.E2.8'], description: 'Compute shape the instance will be built on')
        string(name: 'instance_cpu', defaultValue: '2', description: 'The number of CPUs to provision when using a flex shape')
        string(name: 'instance_memory', defaultValue: '16', description: 'The amount of memory in GBs to provision when using a flex shape')
        string(name: 'base_image', defaultValue: 'stable', description: 'Which base image to use (stable, latest, or specific version number). Use 0 for bare ol7 image')
    }
    stages {
        stage('Validate Input') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_name}"

                    shared.loadValidators().environmentName(params.env_name)

                    image_info = shared.loadImageInfo().info("pgbouncer", params.base_image)
                    println "Base Image OCID: ${image_info.id}"

                    shared.loadInstanceInfo().failOnExists(params.env_name, "pgbouncer", params.env_type)
                }
            }
        }
        stage('Create Server') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }

                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${params.env_type} \
                            -e arg_env_name=${params.env_name} \
                            -e arg_instance_ad=sSxD:${params.instance_ad} \
                            -e arg_instance_shape=${params.instance_shape} \
                            -e arg_jenkins_user=${build_user} \
                            -e "arg_instance_image=${image_info.id}" \
                            -e arg_instance_cpu=${params.instance_cpu} \
                            -e arg_instance_memory=${params.instance_memory} \
                            provisioners/pgbouncer/ansible/create-oci-instance.yaml
                    """
                }
            }
        }
    }
}
