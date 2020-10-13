def getBuildInfoJson(commit_hash) {
    def download_file = pwd() + "/${commit_hash}.info"

    sh(
        script: "oci os object get --namespace idsluxlvwhov --bucket-name app-wars --name ${commit_hash}.info --file ${download_file}",
        returnStdout: true
    ).toString().trim()

    def object_json = readJSON file: download_file

    return object_json
}

def commitExists(commit_hash) {
    return sh(
        script: """oci os object list --namespace idsluxlvwhov --bucket-name app-wars --prefix ${commit_hash}.info | jq '.data | length'""",
        returnStdout: true
    ).toBoolean()
}

def putInfo(commit_hash, status) {
    writeFile file: "${commit_hash}.info", text: """{
"status": "${status}"
}
"""
    upload_file_path = pwd() + "/${commit_hash}.info"
                    
    sh "oci os object put --namespace idsluxlvwhov --bucket-name app-wars --name ${commit_hash}.info --file ${upload_file_path} --force"
}

return this