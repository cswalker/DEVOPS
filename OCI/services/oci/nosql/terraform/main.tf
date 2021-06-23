###Initialization

provider "oci" {
  version      = ">=3.52"
  region       = var.pod_region
  tenancy_ocid = local.oci_vars["tenancy_ocid"]
  auth         = "InstancePrincipal"
}

data "oci_core_services" "regional_services" {}

###Object Storage

terraform {
  #Object storage in OCI where TF State data resides.
  #Terraform does not permit variables here.
  backend "http" {
    ##Expires Jan 19, 2080 16:08 UTC
    address       = "URL TO YOUR TF STATE DATA"
    update_method = "PUT"
  }
}

#OCI variables. Reference with `local.oci_vars["myvar"]["subvar"]`.
locals {
  oci_vars = yamldecode(file("../../../../configurations/oci/vars.yaml"))
}

###Availability Domains

data "oci_identity_availability_domain" "iadad1" {
  compartment_id = local.oci_vars["tenancy_ocid"]
  ad_number      = 1
}

data "oci_identity_availability_domain" "iadad2" {
  compartment_id = local.oci_vars["tenancy_ocid"]
  ad_number      = 2
}

data "oci_identity_availability_domain" "iadad3" {
  compartment_id = local.oci_vars["tenancy_ocid"]
  ad_number      = 3
}
