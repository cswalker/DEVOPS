pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide a URL safe unique name for this environment')
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
                    server_info = instance.info(env_name, "pgbouncer", env_type)
                    println "${server_info.instance.private_ip}"

                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
            }
        }
        stage('Base Install') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[base]
                    ${server_info.instance.private_ip}"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_env_name=${env_name} \
                            provisioners/pgbouncer/ansible/base.yaml
                    """
                }
            }
        }
    }
}
