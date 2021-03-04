def info(env_name, server_name, env_type) {
    failOnNotExists(env_name, server_name, env_type)

    info_file = file(env_name, server_name, env_type)
    info = readYaml file: info_file

    return info
}

def file(env_name, server_name, env_type) {
    return "/home/tomcat/instances/${env_type}/${env_name}/${server_name}.yaml"
}

def failOnExists(env_name, server_name, env_type) {
    if (fileExists(file(env_name, server_name, env_type))) {
        currentBuild.result = 'ABORTED'
        error("${server_name} server for the ${env_name} environment in ${env_type} type already exists")
    }
}

def failOnNotExists(env_name, server_name, env_type) {
    if (!fileExists(file(env_name, server_name, env_type))) {
        currentBuild.result = 'ABORTED'
        error("Environment named '${env_name}' does not have an instance of type '${server_name}' in the '${env_type}' type")
    }
}

def removeInfo(env_name, env_type) {
    sh """rm -rf /home/tomcat/instances/${env_type}/${env_name}"""
}

def getLoadBalancerProfiles(env_name, env_type) {
    def metadata_path = "/home/tomcat/instances/${env_type}/${env_name}/loadbalancer_profiles.yaml"
    if (fileExists(metadata_path)) {
        def data = readYaml file: metadata_path
        return data
    } else {
        return null
    }
}

def getProfileLoadBalancerInfo(profile_name) {
    def metadata_path = "/home/tomcat/profiles/${profile_name}/loadbalancer.yaml"
    if (fileExists(metadata_path)) {
        def data = readYaml file: metadata_path
        return data
    } else {
        return null
    }
}

return this
