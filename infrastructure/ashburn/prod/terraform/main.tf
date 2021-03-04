###Initialization
provider "oci" {
  version          = ">=3.52"
  region           = var.pod_region
  tenancy_ocid     = var.tenancy_ocid
  auth = "InstancePrincipal"
}
data "oci_core_services" "regional_services" {}

###Tags
locals {
  common_params = {
    "compartment" = var.pod_name
    "dc"          = local.common_tags["infra.datacenter"]
    "dns_label"   = var.pod_dns_label
    "env"         = local.common_tags["infra.environment"]
    "pod"         = var.pod_name
    "region"      = local.common_tags["infra.datacenter"]
  }
  common_tags = {
    "infra.datacenter"  = "oci-ash"
    "infra.environment" = var.pod_name
    "infra.customer"    = "internal"
  }
}

###Object Storage
terraform {
  #Object storage in OCI where TF State data resides
  #Terraform does not permit variables here
  backend "http" {
    ## Expires Nov 5, 2080 19:04 UTC
    address       = "Add your Prod Object Storage URL Here"
    update_method = "PUT"
  }
}
#Shared-Resources Terraform Reference
data "terraform_remote_state" "shared_resources" {
  backend = "http"
  config = {
    address = "Add your Shared-Resources Object Storage URL Here"
  }
}

# OCI variables. Reference with `local.oci_vars["myvar"]["subvar"]`.
locals {
  oci_vars = yamldecode(file("../../../../configurations/oci/vars.yaml"))
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
