initialize()

def initialize() {
    shared = load pwd() + "/pipelines/shared/autoload.groovy"
}

def getCommitHash(branch_name) {
    return getCommitHashForRepositoryBranch('switchfly/dev', branch_name)
}

def getCommitHashForRepositoryBranch(repository_path, branch_name) {
    def branch_info = sh(
        script: "git ls-remote git@gitlab.corp.switchfly.com:${repository_path}.git ${branch_name}",
        returnStdout: true
    ).toString().trim()

    def commit_hash
    if (shared.loadValidators().isEmpty(branch_info)) {
        commit_hash = branch_name
    } else {
        commit_hash = branch_info.split('	')[0]
    }

    return commit_hash
}

def getReleaseVersion(commit_hash) {
    def release_info_file = "/home/tomcat/release_versions/${commit_hash}"
    if (!fileExists(release_info_file)) {
        build(job: 'build-get-release-version', parameters: [string(name: 'commit_hash', value: commit_hash)])
    }
    def release_version = sh(
        script: "cat ${release_info_file}",
        returnStdout: true
    ).toString().trim()

    return release_version
}

return this
