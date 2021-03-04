initialize()

def initialize() {
    shared = load pwd() + "/pipelines/shared/autoload.groovy"
}

def getDatabaseInfo(database_name, env_type) {
    def oci_vars = shared.loadOciVars()

    def compartment_ocid = oci_vars.compartments[env_type].compartment_ocid

    def replica_loadbalancer = sh(
        script: """oci nosql query execute --compartment-id ${compartment_ocid} --statement 'SELECT * FROM db_replicas WHERE database_name=\"${database_name}\"'
                """,
        returnStdout: true
    ).toString().trim()

    def lb_info = readJSON text: replica_loadbalancer

    if (lb_info.data.items[0]) {
        return lb_info.data.items[0]
    } else {
        currentBuild.result = 'ABORTED'
        error("No database named [${database_name}] exists for load balancer lookup.")
    }
}

return this
