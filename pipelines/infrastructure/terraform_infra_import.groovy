pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'qa', 'shared-resources', 'uat', 'prod'], description: 'The compartment Terraform is applied against')
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
        stage('Terraform Initialize'){
            steps{
                script {
                    sh "cd infrastructure/ashburn/${env_type}/terraform/ && terraform init"
                }
            }
        }
        stage('Terrafrom Import'){
            steps {
              script {
                  sh "cd infrastructure/ashburn/${env_type}/terraform/ && terraform import ${resource_spec} ${ocid}"
              }
            }
        }
    }
}
