def getInstanceInfo(ocid) {
    def instance_json = sh(
        // list-vnics is considerably slower than list, but it returns the instance info with IP address
        // it also supports getting a specific instance with --instance-id whereas regular list command does not
        script: "oci compute instance list-vnics --instance-id ${ocid}",
        returnStdout: true
    ).toString().trim()
    if (instance_json == "") {
        currentBuild.result = 'ABORTED'
        error("getInstanceInfo: Instance does not exist. ocid: '${ocid}'")
    }
    def instance = readJSON text: instance_json
    return instance.data[0]
}

def getInstanceInfoByName(env_type, env_name, server_name) {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"
    def json_string = sh(
        script: "oci compute instance list --display-name '${env_name}-${server_name}' --compartment-id '${oci_vars.compartments[env_type].compartment_ocid}'",
        returnStdout: true
    ).toString().trim()
    // when there are no resources, the oci command line utility returns an empty string

    def instance
    if (json_string != "") {
        def json_converted = readJSON(text: json_string)
        def instances = json_converted.data.findAll {
            it.get('defined-tags')?.get('Common')?.get('EnvironmentType') == env_type &&
            it.get('defined-tags')?.get('Common')?.get('EnvironmentName') == env_name &&
            !(it['lifecycle-state'].toUpperCase() in ['TERMINATING', 'TERMINATED'])
        }
        if (instances.size() > 1) {
            echo "More than 1 instance was found matching the desired display-name. This could indicate a problem."
        }
        instance = instances[0]
    } else {
        error("getInstanceInfoByName: Instance does not exist. env_type: '${env_type}' env_name: '${env_name}' server_name: '${server_name}'")
    }

    // return only element, if no elements are found, return null
    return instance
}

String[] getInstanceIps(instances) {
    def instance_ips = []

    for (instance in instances) {
        def vnics_json = sh(
            script: "oci compute instance list-vnics --instance-id ${instance.id}",
            returnStdout: true
        )
        def vnics = readJSON text: vnics_json
        instance_ips << vnics.data[0]['private-ip']
    }

    return instance_ips
}

String[] getInstanceIpsByType(env_type, env_name, server_type) {
    def instances = getEnvironmentInstances(env_type, env_name).findAll {
        it['defined-tags']['Instances']['ServerType'] in [server_type]
    }

    return getInstanceIps(instances)
}

String[] getCompartmentInstances(env_type) {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"
    def json_string = sh(
        script: "oci compute instance list --all --compartment-id '${oci_vars.compartments[env_type].compartment_ocid}'",
        returnStdout: true
    ).toString().trim()

    // when there are no resources, the oci command line utility returns an empty string
    def json_converted = json_string != "" ? readJSON(text: json_string) : [data: []]

    return json_converted.data
}

String[] getCompartmentLoadBalancers(env_type) {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"
    def json_string = sh(
        script: "oci lb load-balancer list --all --compartment-id '${oci_vars.compartments[env_type].compartment_ocid}'",
        returnStdout: true
    ).toString().trim()

    // when there are no resources, the oci command line utility returns an empty string
    def json_converted = json_string != "" ? readJSON(text: json_string) : [data: []]

    return json_converted.data
}

def getEnvironmentInstances(env_type, env_name) {
    def all_compartment_instances = getCompartmentInstances(env_type)

    def instances = all_compartment_instances.findAll {
        it['defined-tags']['Common'] != null &&
        it['defined-tags']['Common']['EnvironmentType'] == env_type &&
        it['defined-tags']['Common']['EnvironmentName'] == env_name &&
        !(it['lifecycle-state'].toUpperCase() in ['TERMINATING', 'TERMINATED'])
    }
    return instances
}

def getEnvironmentLoadBalancers(env_type, env_name) {
    def all_compartment_lbs = getCompartmentLoadBalancers(env_type)

    def loadbalancers = all_compartment_lbs.findAll {
        it['defined-tags']['Common'] != null &&
        it['defined-tags']['Common']['EnvironmentType'] == env_type &&
        it['defined-tags']['Common']['EnvironmentName'] == env_name &&
        !(it['lifecycle-state'].toUpperCase() in ['TERMINATING', 'TERMINATED'])
    }
    return loadbalancers
}

def getCompartmentFilesystems(env_type) {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"
    def json_string = sh(
        script: "oci fs file-system list --all --compartment-id '${oci_vars.compartments[env_type].compartment_ocid}' --availability-domain '${oci_vars.regions.ashburn.ads['1']}'",
        returnStdout: true
    ).toString().trim()
    // when there are no resources, the oci command line utility returns an empty string
    def json_converted = json_string != "" ? readJSON(text: json_string) : [data: []]
    return json_converted.data
}

def getEnvironmentFilesystems(env_type, env_name) {
    def filesystems = getCompartmentFilesystems(env_type).findAll {
        it['defined-tags']['Common'] != null &&
        it['defined-tags']['Common']['EnvironmentType'] == env_type &&
        it['defined-tags']['Common']['EnvironmentName'] == env_name &&
        !(it['lifecycle-state'].toUpperCase() in ['TERMINATING', 'TERMINATED'])
    }
    return filesystems
}

def getSubnetInfo(env_type, name) {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"

    def ocid = oci_vars.regions.ashburn[env_type].vcn.subnets[name + '_ocid']

    def json_string = sh(
        script: "oci network subnet get --subnet-id ${ocid}",
        returnStdout: true
    ).toString().trim()

    // when there are no resources, the oci command line utility returns an empty string
    def json_converted = json_string != "" ? readJSON(text: json_string) : [data: []]

    return json_converted.data
}

def failOnExists(env_name, server_name, env_type) {
    echo "Verifying instance ${env_type}-${env_name}-${server_name} does not exist..."
    def success = false
    try {
        getInstanceInfoByName(env_type, env_name, server_name)
    }
    catch (hudson.AbortException e) {
        // instance does not exist
        echo "Success. Instance does not exist"
        success = true
    }

    if (!success) {
        currentBuild.result = 'ABORTED'
        error("${server_name} server for the ${env_name} environment in ${env_type} type already exists")
    }
}

def failOnNotExists(env_name, server_name, env_type) {
    if (getInstanceInfoByName(env_type, env_name, server_name) == null) {
        currentBuild.result = 'ABORTED'
        error("Environment named '${env_name}' does not have an instance of type '${server_name}' in the '${env_type}' type")
    }
}

return this
