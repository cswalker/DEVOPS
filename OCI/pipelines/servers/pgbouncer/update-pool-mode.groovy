pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['prod'], description: 'The environment the server is created in')
        string(name: 'env_name',  defaultValue: '', description: 'Provide a URL safe unique name for this environment')
        choice(name: 'pool_mode', choices: ['session', 'transaction'], description: "Desired pool mode")
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.env_name)
                    
                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name}"
                    pgbouncer_server_info = shared.loadInstanceInfo().info(env_name, "pgbouncer", env_type)
                }
            }
        }
        stage('Updating PgBouncer pool_mode value') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    ${pgbouncer_server_info.instance.private_ip}"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_pool_mode=${pool_mode} \
                            provisioners/pgbouncer/ansible/update-pool-mode.yaml
                    """
                }
            }
        }
    }
}
