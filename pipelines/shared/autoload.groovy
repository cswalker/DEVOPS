def loadInstanceInfo() {
    def instance = load pwd() + "/pipelines/shared/instance.groovy"
    return instance
}

def loadValidators() {
    def validators_obj = load pwd() + "/pipelines/shared/validators.groovy"
    return validators_obj
}

def loadOciVars() {
    def oci_vars = load pwd() + "/pipelines/shared/oci_vars.groovy"
    return oci_vars.getVars()
}

def loadBranchInfo() {
    def branch = load pwd() + "/pipelines/shared/branch_info.groovy"
    return branch
}

def loadBuildInfo() {
    def build = load pwd() + "/pipelines/shared/build.groovy"
    return build
}

def loadImageInfo() {
    def image = load pwd() + "/pipelines/shared/image_info.groovy"
    return image
}

def loadOciBuildObjects() {
    def build_objects = load pwd() + "/pipelines/shared/oci_build_objects.groovy"
    return build_objects
}

def loadOciDynamicInfo() {
    def oci_dynamic_info = load pwd() + "/pipelines/shared/oci_dynamic_info.groovy"
    return oci_dynamic_info
}

def loadReplicaLoadBalancerInfo() {
    def lb_info = load pwd() + "/pipelines/shared/replica_loadbalancer_info.groovy"
    return lb_info
}

return this
