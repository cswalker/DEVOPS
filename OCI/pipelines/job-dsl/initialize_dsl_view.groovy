// Create the job to initialize all other jobs
initialize()

def initialize() {
    pipelineJob("initialize-view") {
        definition {
            logRotator {
                numToKeep(50)
            }
            cpsScm {
                lightweight(true)
                scm {
                    git {
                        branch(branchName)
                        remote {
                            url("INSERT GIT REPO URL HERE")
                        }
                    }
                }
                scriptPath("pipelines/job-dsl/initialize-view.groovy")
            }
        }
    }
}
