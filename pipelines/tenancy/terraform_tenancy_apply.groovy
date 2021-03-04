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
        stage('Terrafrom Apply') {
            steps {
                script {
                    sh "cd infrastructure/tenancy/terraform/ && terraform apply -auto-approve"
                }
            }
        }
    }
}
