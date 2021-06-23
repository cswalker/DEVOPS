###Core networking

##Virtual Cloud Networks
#Creates a Virtual Cloud Network
resource "oci_core_vcn" "pod_vcn" {
  cidr_block     = var.pod_cidr
  compartment_id = oci_identity_compartment.pod_compartment.id
  display_name   = "${var.pod_name} vcn"
  dns_label      = var.pod_dns_label
}

##Service Gateways
#Creates a Service Gateway
resource "oci_core_service_gateway" "pod_sgw" {
  compartment_id = oci_identity_compartment.pod_compartment.id
  display_name   = "${var.pod_name} sgw"
  vcn_id         = oci_core_vcn.pod_vcn.id
  services {
    service_id = data.oci_core_services.regional_services.services.0.id
  }
}

##Local Peering Gateways
#Creates a Local Peering Gateway to the Transit Hub
resource "oci_core_local_peering_gateway" "pod_hub_lpg" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  vcn_id          = oci_core_vcn.pod_vcn.id
  display_name    = "${var.pod_name} to transit-hub lpgw"
}
#Creates a Local Peering Gateway in the Transit-Hub and connects it
resource "oci_core_local_peering_gateway" "hub_pod_lpg" {
  compartment_id  = var.hub_compartment_id
  vcn_id          = data.terraform_remote_state.shared_resources.outputs.hub_vcn_id
  display_name    = "transit-hub to ${var.pod_name} lpgw"
  peer_id         = oci_core_local_peering_gateway.pod_hub_lpg.id
    route_table_id  = data.terraform_remote_state.shared_resources.outputs.hub_lpgw_routetable_id
}
#Creates a Local Peering Gateway to shared-resources for Jenkins Access
resource "oci_core_local_peering_gateway" "pod_sr_lpg" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  vcn_id          = oci_core_vcn.pod_vcn.id
  display_name    = "${var.pod_name} to shared-resources lpgw"
}
#Creates a Local Peering Gateway in the shared-resources and connects it to uat
resource "oci_core_local_peering_gateway" "sr_pod_lpg" {
  compartment_id  = var.hub_compartment_id
  vcn_id          = data.terraform_remote_state.shared_resources.outputs.pod_vcn_id
  display_name    = "shared-resources to ${var.pod_name} lpgw"
  peer_id         = oci_core_local_peering_gateway.pod_sr_lpg.id
}
#Creates a Local Peering Gateway to Corp
resource "oci_core_local_peering_gateway" "pod_corp_lpg" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  vcn_id          = oci_core_vcn.pod_vcn.id
  display_name    = "${var.pod_name} to corp lpgw"
}
#Creates a Local Peering Gateway in corp and connects it to uat
resource "oci_core_local_peering_gateway" "corp_pod_lpg" {
  compartment_id  = local.oci_vars["compartments"]["corp"]["compartment_ocid"]
  vcn_id          = data.terraform_remote_state.corp.outputs.pod_vcn_id
  display_name    = "corp to ${var.pod_name} lpgw"
  peer_id         = oci_core_local_peering_gateway.pod_corp_lpg.id
}

##Internet Gateways
#Creates an Internet Gateway
resource "oci_core_internet_gateway" "pod_igw" {
  compartment_id = oci_identity_compartment.pod_compartment.id
  display_name   = "${var.pod_name} igw"
  vcn_id         = oci_core_vcn.pod_vcn.id
}

##NAT Gateways
#Creates a NAT Gateway
resource "oci_core_nat_gateway" "pod_natgw01" {
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${var.pod_name} natgw 01"
  block_traffic  = "false"
}
