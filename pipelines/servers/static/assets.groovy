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
        choice(name: 'env_type', choices: ['shared'], description: 'The environment the server is created in')
        choice(name: 'env_name', choices: ['static'], description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '01', description: 'Provide a URL safe unique name for this server')
        string(name: 'asset_env_name', defaultValue: '', description: 'Provide the name of the environment for which we are setting up assets')
        string(name: 'profile', defaultValue: '', description: 'Provide the profile name of the environment for which we are setting up assets')
        string(name: 'cdn_url', defaultValue: 'uat205cdn.switchfly.com', description: 'Provide the name of the environment for which we are setting up assets')
        string(name: 'commit_hash', defaultValue: '', description: 'The commit hash to set up assets for')
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

                    currentBuild.displayName = "#${BUILD_NUMBER} ${commit_hash.take(7)}"

                    oci_object = load pwd() + "/pipelines/shared/oci_build_objects.groovy"
                    compile_info = oci_object.getBuildInfoJson(commit_hash)
                }
            }
        }
        stage('Set up Assets') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_name=${env_name} \
                            -e arg_env_type=${env_type} \
                            -e arg_server_name=${server_name} \
                            -e arg_commit_hash=${commit_hash} \
                            -e arg_asset_env_name=${asset_env_name} \
                            -e arg_profile=${profile} \
                            -e arg_cdn_url=${cdn_url} \
                            -e arg_cdn_assets_url=${compile_info.cdn_assets_url} \
                            provisioners/static/ansible/assets.yaml
                    """
                }
            }
        }
    }
}
