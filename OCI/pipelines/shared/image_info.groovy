String[] getAllImages() {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"
    def json_string = sh(
        script: "oci compute image list --all --compartment-id '${oci_vars.compartments.switchfly_infra_storage.compartment_ocid}' --auth instance_principal",
        returnStdout: true
    ).toString().trim()

    // when there are no resources, the oci command line utility returns an empty string
    def json_converted = json_string != "" ? readJSON(text: json_string) : [data: []]

    return json_converted.data
}

String[] getImagesByType(server_type) {
    def server_images = getAllImages().findAll {
        it['defined-tags']['Images'] != null &&
        it['defined-tags']['Images']['ServerType'] == server_type
    }

    return server_images
}

String[] getImageByAlias(server_type, alias) {
    def image = getImagesByType(server_type).findAll {
        it['defined-tags']['Images'] != null &&
        it['defined-tags']['Images'][alias] == 'true'
    }

    imageNotFound(image[0], server_type, alias)
    
    return image[0]
}

def getImageByVersion(server_type, version) {
    if (version == "latest") {
        return getImageByAlias(server_type, "IsLatest")
    }

    if (version == "stable") {
        return getImageByAlias(server_type, "IsStable")
    }

    def image = getAllImages().findAll {
        it['defined-tags']['Images'] != null &&
        it['defined-tags']['Images']['ServerType'] == server_type &&
        it['defined-tags']['Images']['Version'] == "${version}"
    }

    imageNotFound(image[0], server_type, version)

    return image[0]
}

def getNextImageVersion(server_type) {
    def greatest = 0
    getImagesByType(server_type).each { value ->
        def current = value.'defined-tags'.Images.Version.toInteger()
        
        if (current > greatest) {
            greatest = current
        }
    }

    return greatest + 1
}

def unsetAlias(server_type, alias) {
    def image = getImagesByType(server_type).findAll {
        it['defined-tags']['Images'][alias] == "true"
    }

    println "========= Unset ${alias} for ${server_type} ========="
    println image

    if (!image.isEmpty() && image != "" && image != null ) {
        setTagValue(image[0].id, alias, "false", image[0].'defined-tags')
    }
}

def setTagValue(ocid, tag_key, tag_value, tags) {
    tags['Images'][tag_key] = tag_value


    println "========= Set tag ${tag_key} to ${tag_value} for ${ocid} ========="

    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"
    def json_string = sh(
        script: "oci compute image update --image-id '${ocid}' --defined-tags '${definedTagsToJson(tags)}' --force --auth instance_principal",
        returnStdout: true
    ).toString().trim()
}

def setVersionAlias(server_type, version, alias) {
    def image = getImagesByType(server_type).findAll {
        it['defined-tags']['Images']['Version'] == "${version}"
    }

    unsetAlias(server_type, alias)
    setTagValue(image[0].id, alias, "true", image[0].'defined-tags')
}

String definedTagsToJson(tags) {
    def defined_tags = """{\"Images\": 
        {
            \"IsLatest\": \"${tags.Images.IsLatest}\",
            \"IsStable\": \"${tags.Images.IsStable}\",
            \"ServerType\": \"${tags.Images.ServerType}\",
            \"Version\": \"${tags.Images.Version}\"
        } 
    }"""

    return defined_tags
}

def imageNotFound(image, server_type, version = '') {
    if (image == null) {
        currentBuild.result = 'ABORTED'
        error("Base image [${version}] does not exist for ${server_type}")
    }
}

def printDetails(image) {
    def image_details = """
----------------------------------------------------
OCID: ${image.id}
ServerType: ${image.'defined-tags'.Images.ServerType}
Version: ${image.'defined-tags'.Images.Version}
IsStable: ${image.'defined-tags'.Images.IsStable}
IsLatest: ${image.'defined-tags'.Images.IsLatest}
"""

    println image_details
}

// This wrapper function is used by any pipeline that gets the base image info
def info(server, version) {
    info = getImageByVersion(server, version)
    return info
}

def cleanup() {
    manifest_file = pwd() + '/manifest.json'
    sh "rm -f ${manifest_file}"
}

def setStable(server, version) {
    // always get the image details because an alias or invalid version number could be provided
    // getImageByVersion will fail if the version is invalid or does not exist
    def image = getImageByVersion(server, version)

    setVersionAlias(server, image.'defined-tags'.Images.Version, "IsStable")
}

return this
