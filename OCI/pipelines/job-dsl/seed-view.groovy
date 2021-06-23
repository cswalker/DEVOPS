pipeline {
    agent any
    parameters {
        string(name: 'branch', defaultValue: env.branch ?: "", description: 'Branch to be used on the created pipelines')
        string(name: 'directory', defaultValue: '', description: 'The sub-directory of pipelines to seed, for example: servers/app')
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

                    jobDsl targets: ['pipelines/job-dsl/seed_dsl.groovy', 'pipelines/job-dsl/initialize_dsl_view.groovy'].join("\n"),
                        lookupStrategy: 'SEED_JOB',
                        additionalParameters: [branchName: branch, relativePaths: pipelines.suppliedPaths(params.directory), views: [params.directory]]
                    
                    build job: 'initialize-view',
                        parameters: [
                            string(name: 'path', value: params.directory)
                        ],
                        quietPeriod: 0
                }

            }
        }
    }
}
