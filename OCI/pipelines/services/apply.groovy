pipeline {
    agent any
    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'service', choices: ['nosql', 'vault'], description: 'Which service to update')
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
                    sh "cd services/oci/${service}/terraform/ && terraform init"
                }
            }
        }
        stage('Terrafrom Apply') {
            steps {
              script {
                  sh "cd services/oci/${service}/terraform/ && terraform apply -auto-approve"
              }
            }
        }
    }
}
