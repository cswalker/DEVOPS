pipeline {
    agent any
    parameters {
        string(name: 'branch', defaultValue: env.branch ?: "", description: 'Branch to be used on the created pipelines')
    }
    stages {
        stage('Init') {
            when {
                expression { env.branch == null || env.branch.trim() == "" }
            }
            steps {
                script {
                    currentBuild.result = 'ABORTED'
                    error("Pipeline initialized")
                }
            }
        }
        stage('Process Job DSLs') {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER} ${branch}"

                    def pipelines = load pwd() + "/pipelines/job-dsl/pipelines.groovy"

                    jobDsl targets: ['pipelines/job-dsl/seed_dsl.groovy', 'pipelines/job-dsl/initialize_dsl.groovy'].join("\n"),
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'SEED_JOB',
                        additionalParameters: [branchName: branch, relativePaths: pipelines.getPaths(), views: pipelines.getDirs()]
                    build job: 'initialize', quietPeriod: 0
                }
            }
        }
    }
}
