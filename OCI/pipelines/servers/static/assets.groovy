pipeline {
    agent any

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['shared'], description: 'The environment the server is created in')
        choice(name: 'env_name', choices: ['static'], description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
        string(name: 'profile', defaultValue: '', description: 'Provide the profile name of the environment for which we are setting up assets')
        string(name: 'commit_hash', defaultValue: '', description: 'The commit hash to set up assets for')
        booleanParam(name: 'overwrite', defaultValue: false, description: 'Delete current assets dir and recreate using archive packages.')
        string(name: 'env_configuration_file', defaultValue: '', description: 'Provide the configuration file path for the environment')
        booleanParam(name: 'trigger_asset_cleanup', defaultValue: true, description: 'Trigger cleanup of old assets (non blocking)')
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
                    server_info = instance.info(env_name, server_name, env_type)

                    println "${server_info.instance.private_ip}"

                    currentBuild.displayName = "#${BUILD_NUMBER} ${server_name} ${commit_hash.take(7)}"

                    oci_object = load pwd() + "/pipelines/shared/oci_build_objects.groovy"
                    compile_info = oci_object.getBuildInfoJson(commit_hash)
                }
            }
        }
        stage('Set up Assets') {
            steps {
                lock(resource: "${commit_hash}-cdn-assets") {
                    script {
                        writeFile file: 'inventory.yaml', text: """[${env_type}]
                        ${server_info.instance.private_ip}"""
                        sh """
                            ansible-playbook -i inventory.yaml \
                                -e arg_env_name=${env_name} \
                                -e arg_env_type=${env_type} \
                                -e arg_server_name=${server_name} \
                                -e arg_commit_hash=${commit_hash} \
                                -e arg_profile=${profile} \
                                -e arg_cdn_assets_url=${compile_info.cdn_assets_tar_url} \
                                -e arg_cdn_static_url=${compile_info.cdn_static_tar_url} \
                                -e arg_overwrite=${overwrite} \
                                -e arg_configuration_file=${env_configuration_file} \
                                provisioners/static/ansible/assets.yaml
                        """
                    }
                }
            }
        }
        stage('Delete old assets') {
            when {
                expression { trigger_asset_cleanup == 'true' }
            }
            steps {
                build job: 'static-assets-cleanup',
                    parameters: [
                        string(name: 'env_type', value: env_type),
                        string(name: 'env_name', value: env_name),
                        string(name: 'server_name', value: server_name),
                        string(name: 'profile', value: profile),
                    ],
                    wait: false
            }
        }
    }
}
