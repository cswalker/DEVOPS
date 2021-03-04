pipeline {
    agent any

    environment {
        LC_ALL = 'en_US.UTF-8'
        LANG    = 'en_US.UTF-8'
        LANGUAGE = 'en_US.UTF-8'
        OCI_CLI_AUTH = 'instance_principal'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['shared'], description: 'Compartment to create the server in')
        string(name: 'env_name', defaultValue: 'yum', description: 'Provide a URL safe unique name for this environment')
        choice(name: 'instance_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availibility domain the instance will be created in')
        choice(name: 'instance_shape', choices: ['VM.Standard.E2.1'], description: "Instance shape for the server")
    }

    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name}"

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                }
            }
        }
        stage('Create') {
            steps {
                build job: 'yum-mirror-create-oci-instance',
                    parameters: [
                        string(name: 'env_type', value: env_type),
                        string(name: 'env_name', value: "${env_name}"),
                        string(name: 'instance_shape', value: "${instance_shape}"),
                        string(name: 'instance_ad', value: "${instance_ad}"),
                    ]
            }
        }
        stage('Gather Instance Info') {
            steps {
                script {
                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(env_name, 'mirror', env_type)
                }
            }
        }
        stage('Configure Yum Mirror') {
            steps {
                build job: 'yum-mirror-build',
                    parameters: [
                        string(name: 'env_type', value: env_type),
                        string(name: 'env_name', value: "${env_name}"),
                    ]

            }
        }
        stage('Add DNS Entry') {
            steps {
                build(job: 'dns-update-record-internal',
                    parameters: [
                        string(name: 'env_type', value: env_type),
                        string(name: 'record', value: "${env_name}-mirror"),
                        string(name: 'ip_address', value: "${server_info.instance.private_ip}")
                    ]
                )
            }
        }
    }
}
