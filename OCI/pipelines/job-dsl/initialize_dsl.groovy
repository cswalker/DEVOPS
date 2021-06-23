// Create the job to initialize all other jobs
initialize()

def initialize() {
    pipelineJob("initialize") {
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
                            credentials('Insert Instance Pincipal GIT SSH Credentials')
                        }
                    }
                }
                scriptPath("pipelines/job-dsl/initialize.groovy")
            }
        }
    }
}
