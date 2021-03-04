pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
    }
    stages {
        stage('Init') {
            steps {
                script {
                    validators = load pwd() + "/pipelines/shared/validators.groovy"
                    validators.init(params.job)

                    image = load pwd() + "/pipelines/shared/image_info.groovy"
                    image.cleanup()
                }
            }
        }
        stage('Create Image') {
            steps {
                script {
                    sh """
                        /usr/local/bin/packer build provisioners/couchbase/packer/oci-base-image.json
                    """
                }
            }
        }
        stage('Save Image Info') {
            steps {
                script {
                    image_ocid = image.ocid()
                    count = image.count('couchbase')
                    image.build('couchbase', count, image_ocid)
                }
            }
        }
    }
}
