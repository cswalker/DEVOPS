pipeline {
    agent any

    environment {
        env_type = 'prod'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'prod', 'shared', 'corp'], description: 'The environment the server is created in')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availability domain the instance will be created in')
        choice(name: 'instance_role', choices: ['primary', 'secondary'], description: 'The Role of the Domain Controller Instance')
        choice(name: 'instance_shape', choices: ['VM.Standard.E4.Flex'], description: "Instance shape for the app servers")
        string(name: 'instance_cpu', defaultValue: '2', description: 'The number of CPUs to provision when using a flex shape')
        string(name: 'instance_memory', defaultValue: '8', description: 'The amount of memory in GBs to provision when using a flex shape')
        string(name: 'base_image', defaultValue: 'stable', description: 'Which base image to use (stable, latest, or specific version number). Use 0 for bare ol7 image')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
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
        stage('Validate Input') {
            steps {
                script {
                    server_info = shared.loadInstanceInfo().info(env_name, server_name, params.env_type)
                    println "${server_info.instance.private_ip}"
                }
            }
        }
        stage('Detach DNS Volume') {
            steps {
                script {
                    build job: 'dns-detach-oci-volume',
                        parameters: [
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'instance_ad', value: "${params.instance_ad}"),
                            string(name: 'instance_role', value: "${params.instance_role}"),
                        ]
                }
            }
        }
        stage('Destroy DNS Instance') {
            steps {
                script {
                    build job: 'dns-destroy-oci-instance',
                        parameters: [
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'instance_ad', value: "${params.instance_ad}"),
                            string(name: 'instance_role', value: "${params.instance_role}"),
                        ]
                }
            }
        }
        stage('Create New DNS Instance') {
            steps {
                script {
                    build job: 'dns-create-oci-instance',
                        parameters: [
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'instance_ad', value: "${params.instance_ad}"),
                            string(name: 'instance_role', value: "${params.instance_role}"),
                            string(name: 'instance_shape', value: "${params.instance_shape}"),
                            string(name: 'instance_cpu', value: "${params.instance_cpu}"),
                            string(name: 'instance_memory', value: "${params.instance_memory}"),
                            string(name: 'base_image', value: "${params.base_image}"),
                        ]
                }
            }
        }
        stage('Attach DNS Volume') {
            steps {
                script {
                    build job: 'dns-attach-oci-volume',
                        parameters: [
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'instance_ad', value: "${params.instance_ad}"),
                            string(name: 'instance_role', value: "${params.instance_role}"),
                        ]
                }
            }
        }
        stage('Configure DNS Instance') {
            steps {
                script {
                    build job: 'dns-configure-dns',
                        parameters: [
                            string(name: 'env_type', value: params.env_type),
                            string(name: 'instance_role', value: "${params.instance_role}"),
                        ]
                }
            }
        }
    }
}
