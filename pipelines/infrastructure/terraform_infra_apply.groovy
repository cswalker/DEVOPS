pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['init', 'run'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['dev', 'shared-resources'], description: 'The compartment Terraform is applied against')
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
        stage('Terrafrom Apply'){
            steps {
              script {
                  sh "cd infrastructure/ashburn/${env_type}/terraform/ && terraform apply -auto-approve"
              }
            }
        }
    }
}
