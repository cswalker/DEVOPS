def getDirs() {
    def directories = [
        "servers/checkmk",
        "servers/common",
        "servers/couchbase",
        "servers/dns",
        "servers/domain-controller",
        "servers/jenkins",
        "servers/loadbalancer",
        "servers/loadproxy",
        "servers/loadtest",
        "servers/pgbouncer",
        "servers/splunk",
        "servers/static",
        "servers/syslog",
        "servers/yum-mirror",
    ]

    return directories
}

def suppliedPaths(String path) {
    def pipelinePaths = []

    def foundFiles = findFiles(glob: "pipelines/${path}/**/*.groovy")
    foundFiles.each { element ->
        pipelinePaths << element.path
    }

    return pipelinePaths
}

def getPaths() {
    def directories = getDirs()

    def individualFiles = [
        "pipelines/utilities/environment-report.groovy",
    ]

    def pipelinePaths = []
    directories.each { path ->
        def foundFiles = findFiles(glob: "pipelines/${path}/**/*.groovy")
        foundFiles.each { element ->
            pipelinePaths << element.path
        }
    }

    individualFiles.each { path ->
        pipelinePaths << path
    }
    return pipelinePaths
}

def getJobNameFromPath(String path) {
    def pathParts = path.split('/')
    def jobName = pathParts[-2] + "-" + pathParts[-1].replace('.groovy', '')
    return jobName
}

return this
