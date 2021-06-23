pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'block_volume_restore_env_type', choices: ['dev', 'qa', 'prod', 'uat', 'shared'], description: 'The environment the backup is restored in')
        string(name: 'block_volume_restore_env_name', defaultValue: '', description: 'Provide the environment name the backup is restored in')
        choice(name: 'block_volume_restore_ad', choices: ['US-ASHBURN-AD-1', 'US-ASHBURN-AD-2', 'US-ASHBURN-AD-3'], description: 'Availability domain the backup will be restored in')
        string(name: 'block_volume_restore_display_name', defaultValue: '', description: 'Provide the name of the restored volume')
        string(name: 'block_volume_restore_volume_id', defaultValue: '', description: 'Provide the OCID of backed volume to be restored')
        string(name: 'block_volume_restore_compartment_id', defaultValue: '', description: 'Provide the OCID of compartment backup to be restored')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)
                    validators.environmentName(params.block_volume_restore_env_name)
                }
            }
        }
        stage('Backup') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${block_volume_restore_env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_block_volume_restore_env_type=${block_volume_restore_env_type} \
                            -e arg_block_volume_restore_env_name=${block_volume_restore_env_name} \
                            -e arg_block_volume_restore_ad=sSxD:${block_volume_restore_ad} \
                            -e arg_block_volume_restore_display_name=${block_volume_restore_display_name} \
                            -e arg_block_volume_restore_volume_id=${block_volume_restore_volume_id} \
                            -e arg_block_volume_restore_compartment_id=${block_volume_restore_compartment_id} \
                            provisioners/common/ansible/block-volume-restore.yaml
                    """
                }
            }
        }
    }
}
