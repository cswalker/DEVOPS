pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'shared'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name')
        string(name: 'server_name', defaultValue: '', description: 'Provide a URL safe unique name for this server')
        choice(name: 'policy_type', choices: ['gold', 'silver', 'bronze'], description: 'What level of backup policy to apply to this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    instance = load pwd() + "/pipelines/shared/instance.groovy"
                    server_info = instance.info(env_name, server_name, env_type)
                    server_exists = fileExists(instance.file(env_name, server_name, env_type))
                }
            }
        }
        stage('Apply Backup Policy') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                }
                script {
                    writeFile file: 'inventory.yaml', text: """[${env_type}]
                    localhost"""
                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_instance_id=${server_info.instance.id} \
                            -e arg_compartment=${server_info.instance.compartment} \
                            -e arg_ad_id=${server_info.instance.ad} \
                            -e arg_policy_type=${policy_type} \
                            -e arg_jenkins_user=${build_user} \
                            provisioners/common/ansible/boot-backup.yaml
                    """
                }
            }
        }
    }
}
