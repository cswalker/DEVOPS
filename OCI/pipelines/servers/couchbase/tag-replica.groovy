pipeline {
    agent any

    environment {
        env_type = 'prod'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        string(name: 'server_name', defaultValue: '', description: 'The name of the server to tag')
        string(name: 'replication_source', defaultValue: '', description: 'The server name that is being replicated')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.env_name}"

                    server_info = shared.loadInstanceInfo().info(env_name, server_name, env_type)
                }
            }
        }
        stage('Tag Primary Couchbase Server') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_type=${env_type} \
                            -e arg_env_name=${env_name} \
                            -e arg_server_name=${server_name} \
                            -e arg_replication_source=${replication_source} \
                            provisioners/couchbase/ansible/tag-replica.yaml
                    """
                }
            }
        }
    }
}
