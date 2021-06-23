###Initialization
provider "oci" {
  version      = ">=4.3"
  tenancy_ocid = var.tenancy_ocid
  region       = var.pod_region
  auth         = "InstancePrincipal"
}

###Object Storage
terraform {
  #Object storage in OCI where TF State data resides
  #Terraform does not permit variables here
  backend "http" {
    ## Expires Nov 19, 2080 21:52 UTC
    address       = "Add OCID for Tenance Object Storage Here"
    update_method = "PUT"
  }
}

#OCI variables. Reference with `local.oci_vars["myvar"]["subvar"]`.
locals {
  oci_vars = yamldecode(file("../../../configurations/oci/vars.yaml"))
}
