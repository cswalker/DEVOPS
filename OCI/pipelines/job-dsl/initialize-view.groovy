pipeline {
    agent any
    parameters {
        string(name: 'path', defaultValue: '', description: 'The path to initialize')
    }
    stages {
        stage('Initialize Jobs') {
            steps {
                script {
                    pipelines = load pwd() + "/pipelines/job-dsl/pipelines.groovy"

                    def initJobs = [:]
                    pipelines.suppliedPaths(params.path).each { path ->
                        def jobName = pipelines.getJobNameFromPath(path)
                        initJobs[jobName] = {
                            result = build(job: jobName, parameters: [
                                    string(name: 'job', value: 'init')
                            ], propagate: false, quietPeriod: 0)
                            if (result.getResult() == 'ABORTED') {
                                echo "${jobName} initialized"
                            } else {
                                echo "${jobName} initialization failed. Expected result 'ABORTED'. Got '${result}'"
                                currentBuild.result = 'FAILURE'
                            }
                        }
                    }
                    parallel initJobs
                }
            }
        }
    }
}
