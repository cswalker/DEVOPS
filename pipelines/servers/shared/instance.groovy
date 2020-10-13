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

def profileConfiguration(profile_name, profile_configuration, env_name, env_type) {
    if (profile_name != "previous") {
        saveConfiguration(profile_name, profile_configuration, env_name, env_type)
    }

    configuration_file = getConfiguration(env_name, env_type)
    return configuration_file
}

def getConfiguration(env_name, env_type) {
    configuration_file = "/home/tomcat/instances/${env_type}/${env_name}/configuration.yaml"
    if (!fileExists(configuration_file)) {
        currentBuild.result = 'ABORTED'
        error("No previous profile configuration exists. Please choose from a static profile or add a custom profile configuration.")
    }

    return configuration_file
}

def saveConfiguration(profile_name, profile_configuration, env_name, env_type) {
    configuration_file = "/home/tomcat/instances/${env_type}/${env_name}/configuration.yaml"

    if (profile_name == 'custom') {
        writeFile file: configuration_file, text: "${profile_configuration}"
    } else {
        standard_configuration_file = pwd() + "/configurations/${env_type}/${profile_name}.yaml"
        profile_configuration = readFile file: standard_configuration_file
        writeFile file: configuration_file, text: "${profile_configuration}"
    }
}

def readConfiguration(env_type, env_name) {
    def metadata_path = "/home/tomcat/instances/${env_type}/${env_name}"
    config = readYaml file: "${metadata_path}/configuration.yaml"
    return config
}

def writeState(env_type, env_name, state) {
    def metadata_path = "/home/tomcat/instances/${env_type}/${env_name}"
    // writeYaml won't overwrite files.
    // a new 'overwrite' parameter has been added (and is described in docs) but our version doesn't have it yet
    sh "rm -f ${metadata_path}/state_new.yaml"
    writeYaml file: "${metadata_path}/state_new.yaml", data: state
    sh "mv ${metadata_path}/state_new.yaml ${metadata_path}/state.yaml"

    echo "Wrote state.yaml for env_type '${env_type}' env_name '${env_name}': \n ${state}"
}

def readState(env_type, env_name) {
    def metadata_path = "/home/tomcat/instances/${env_type}/${env_name}"
    state = readYaml file: "${metadata_path}/state.yaml"
    return state
}

def saveActiveGroup(env_name, env_type, active_group) {
    active_group_file = "/home/tomcat/instances/${env_type}/${env_name}/active_group.yaml"
    sh "rm -rf ${active_group_file}"

    def data = [active_group: active_group]
    writeYaml file: active_group_file, data: data 
}

def getActiveGroup(env_name, env_type) {
    active_group_file = "/home/tomcat/instances/${env_type}/${env_name}/active_group.yaml"
    data = readYaml file: active_group_file
    return data.active_group
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
