pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'uat', 'shared', 'prod', 'corp'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'Provide the environment name')
        string(name: 'server_name', defaultValue: '', description: 'Provide a URL safe unique name for this server')
        choice(name: 'policy_type', choices: ['gold', 'silver', 'bronze'], description: 'What level of backup policy to apply to this server')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.env_name)

                    server_info = shared.loadInstanceInfo().info(params.env_name, params.server_name, params.env_type)
                    println "${server_info.instance.private_ip}"

                    shared.loadInstanceInfo().failOnNotExists(params.env_name, params.server_name, params.env_type)
                }
            }
        }
        stage('Apply Backup Policy') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }
                    writeFile file: 'inventory.yaml', text: """[${params.env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""
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
