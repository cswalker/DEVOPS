def count(server) {
    count_file = "/home/tomcat/images/${server}/count.yaml"
    
    if (!fileExists(count_file)) {
        count = 1
    } else {
        count_map = readYaml file: count_file
        count = count_map.count + 1
        sh "rm -f ${count_file}"
    }
    
    writeYaml file: count_file, data: ['count': count]
    return count
}

def build(server, count, image_ocid) {
    writeYaml file: "/home/tomcat/images/${server}/${count}.yaml", data: ['id': image_ocid]

    sh "ln -sf /home/tomcat/images/${server}/${count}.yaml /home/tomcat/images/${server}/latest.yaml"
}

def ocid() {
    manifest = readJSON file: pwd() + '/manifest.json'
    return manifest.builds[0].artifact_id
}

def info(server, version) {
    image_info_file = "/home/tomcat/images/${server}/${version}.yaml"

    if (!fileExists(image_info_file)) {
        currentBuild.result = 'ABORTED'
        error("Base image [${version}] does not exist for ${server}")
    }

    info = readYaml file: image_info_file
    return info
}

def cleanup() {
    manifest_file = pwd() + '/manifest.json'
    sh "rm -f ${manifest_file}"
}

def setStable(server, version) {
    if (version == "latest") {
        version_file = sh (script: "readlink /home/tomcat/images/${server}/latest.yaml", returnStdout: true).trim()
    } else {
        version_file = "/home/tomcat/images/${server}/${version}.yaml"
    }

    sh "ln -sf ${version_file} /home/tomcat/images/${server}/stable.yaml"
}

return this