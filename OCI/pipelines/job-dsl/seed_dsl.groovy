// Create the pipelines
relativePaths.each { path ->
    def pathParts = path.split('/')
    def name = pathParts[-2] + "-" + pathParts[-1].replace('.groovy', '')
    pipeline([relativePath: path, jobName: name])
}

// Create the views
views.each { dir ->
    def dirParts = dir.split('/')
    def groupName = dirParts[-1]

    views(groupName)
}

def pipeline(Map options) {
    pipelineJob(options.jobName) {
        definition {
            logRotator {
                daysToKeep(JOB_NAME =~ /^(stable|master)\// ? 90 : 7)
                numToKeep(JOB_NAME =~ /^(stable|master)\// ? 200 : 50)
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
                scriptPath(options.relativePath)
            }
        }
    }
}

def views(String groupName) {
    listView(groupName) {
        filterBuildQueue()
        filterExecutors()
        jobs {
            regex('^' + groupName + '-.*$')
        }
        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
        }
    }
}
