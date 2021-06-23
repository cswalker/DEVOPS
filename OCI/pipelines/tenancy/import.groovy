pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        string(name: 'resource_spec', description: 'Resource spec. Example: resource_type.resource_name[resource index]')
        string(name: 'ocid', description: 'OCID of the resource to import')
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
        stage('Terraform Initialize') {
            steps {
                script {
                    sh "cd infrastructure/tenancy/terraform/ && terraform init"
                }
            }
        }
        stage('Terrafrom Import') {
            steps {
                script {
                    sh "cd infrastructure/tenancy/terraform/ && terraform import ${resource_spec} ${ocid}"
                }
            }
        }
    }
}
