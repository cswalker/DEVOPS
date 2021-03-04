def getVars() {
    def oci_vars = readYaml file: pwd() + "/configurations/oci/vars.yaml"

    return oci_vars
}

return this
