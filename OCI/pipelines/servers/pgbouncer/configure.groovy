pipeline {
    agent any

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'The environment name to build the app from')
        string(name: 'profile_name', defaultValue: 'previous', description: 'Enter the predefined profile configuration name or enter [custom] to enter a new configuration. Default of [previous] will use the configuration established during creation')
        text(name: 'profile_configuration', defaultValue: '', description: 'Custom configuration yaml file')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name}"

                    validators.environmentName(params.env_name)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    pgbouncer_server_info = instance.info(env_name, "pgbouncer", env_type)

                    configuration_file = instance.profileConfiguration(profile_name, profile_configuration, env_name, env_type)

                    if (env_type == 'qa') {
                        db_server_info = instance.info(env_name, "db", env_type)
                        db_ip_args = "-e arg_db_primary_ip=${db_server_info.instance.private_ip}"
                    } else if (env_type == 'uat') {
                        db01_server_info = instance.info(env_name, "db01", env_type)
                        db02_server_info = instance.info(env_name, "db02", env_type)
                        db_ip_args = "-e arg_db_primary_ip=${db01_server_info.instance.private_ip} \
                            -e arg_db_replica_ip=${db02_server_info.instance.private_ip}"
                    } else if (env_type == 'prod') {
                        db01_server_info = instance.info(env_name, "db01", env_type)
                        db02_server_info = instance.info(env_name, "db02", env_type)
                        db03_server_info = instance.info(env_name, "db03", env_type)
                        db04_server_info = instance.info(env_name, "db04", env_type)
                        db_ip_args = "-e arg_db01_primary_ip=${db01_server_info.instance.private_ip} \
                            -e arg_db02_replica_ip=${db02_server_info.instance.private_ip} \
                            -e arg_db03_replica_ip=${db03_server_info.instance.private_ip} \
                            -e arg_db04_replica_ip=${db04_server_info.instance.private_ip}"

                    }

                    use_tx_pool_mode = true
                    def state = instance.readState(env_type, env_name)
                    if ((state.release_version as Integer) < 211) {
                        use_tx_pool_mode = false
                        echo "Overriding use_tx_pool_mode flag due to unsupported release version: ${state.release_version}"
                    }
                }
            }
        }
        stage('Configure PgBouncer') {
            steps {
                script {
                    script {
                        writeFile file: 'inventory.yaml', text: """[${env_type}]
                        ${pgbouncer_server_info.instance.private_ip}"""

                        sh """
                            ansible-playbook -i inventory.yaml \
                                -e arg_env_type=${env_type} \
                                -e arg_env_name=${env_name} \
                                ${db_ip_args} \
                                -e arg_configuration_file=${configuration_file} \
                                -e arg_use_tx_pool_mode=${use_tx_pool_mode} \
                                provisioners/pgbouncer/ansible/configure.yaml
                        """
                    }
                }
            }
        }
    }
}
