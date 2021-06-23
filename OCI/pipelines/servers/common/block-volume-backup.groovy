pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'block_volume_backup_env_type', choices: ['dev', 'qa', 'prod', 'uat', 'shared'], description: 'The environment the backup is created in')
        string(name: 'block_volume_backup_env_name', defaultValue: '', description: 'Provide the environment name the backup is created in')
        string(name: 'block_volume_backup_display_name', defaultValue: '', description: 'Provide the name of backup')
        string(name: 'block_volume_backup_volume_id', defaultValue: '', description: 'Provide the OCID of block volume to be backed up')
        string(name: 'block_volume_backup_compartment_id', defaultValue: '', description: 'Provide the compartment OCID the backup will be created in')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)
                    validators.environmentName(params.block_volume_backup_env_name)
                }
            }
        }
        stage('Backup') {
            steps {
                script {
                    writeFile file: 'inventory.yaml', text: """[${block_volume_backup_env_type}]
                    localhost ansible_python_interpreter=/usr/bin/python3"""

                    sh """
                        ansible-playbook -i inventory.yaml \
                            -e arg_block_volume_backup_env_type=${block_volume_backup_env_type} \
                            -e arg_block_volume_backup_env_name=${block_volume_backup_env_name} \
                            -e arg_block_volume_backup_display_name=${block_volume_backup_display_name} \
                            -e arg_block_volume_backup_volume_id=${block_volume_backup_volume_id} \
                            -e arg_block_volume_backup_compartment_id=${block_volume_backup_compartment_id} \
                            provisioners/common/ansible/block-volume-backup.yaml
                    """
                }
            }
        }
    }
}
