###Initialization
provider "oci" {
  version          = ">=3.52"
  region           = var.pod_region
  tenancy_ocid     = var.tenancy_ocid
  auth = "InstancePrincipal"
}
data "oci_core_services" "regional_services" {}

###Object Storage
terraform {
  #Object storage in OCI where TF State data resides
  #Terraform does not permit variables here
  backend "http" {
    ## Expires Aug 12, 2080 16:15 UTC
    address       = "Insert URL to shared-resources Terraform State Object Storage Here"
    update_method = "PUT"
  }
}
#Dev Terraform Reference
data "terraform_remote_state" "dev" {
  backend = "http"
  config = {
    address = "Insert URL to Dev Terraform State Object Storage Here"
  }
}

###Availability Domains
#Set Availability Domains 1, 2, & 3
data "oci_identity_availability_domain" "iadad1" {
  compartment_id = var.tenancy_ocid
  ad_number      = 1
}
data "oci_identity_availability_domain" "iadad2" {
  compartment_id = var.tenancy_ocid
  ad_number      = 2
}
data "oci_identity_availability_domain" "iadad3" {
  compartment_id = var.tenancy_ocid
  ad_number      = 3
}