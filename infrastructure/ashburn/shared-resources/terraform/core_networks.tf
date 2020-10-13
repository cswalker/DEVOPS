###Core networking

##Virtual Cloud Networks
#Creates a Virtual Cloud Network for the shared-resources network
resource "oci_core_vcn" "pod_vcn" {
  cidr_block     = var.pod_cidr
  compartment_id = oci_identity_compartment.pod_compartment.id
  display_name   = "${var.pod_name} vcn"
  dns_label      = var.pod_dns_label
}
#Creates a Virtual Cloud Network for the Transit-Hub network
resource "oci_core_vcn" "hub_vcn" {
  cidr_block      = var.hub_cidr
  compartment_id  = oci_identity_compartment.pod_compartment.id
  display_name    = "${var.hub_name} vcn"
  dns_label       = var.hub_dns_label
}

##Dynamic Routing Gateways
#Creates the Transit Hub Dynamic Routing Gateway
resource "oci_core_drg" "hub_drg" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  display_name    = "transit-hub drg"
}


##Service Gateways
#Creates a Service Gateway for the Shared-Resources Virtual Cloud Network
resource "oci_core_service_gateway" "pod_sgw" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  display_name    = "${var.pod_name} sgw"
  vcn_id          = oci_core_vcn.pod_vcn.id
  services {
    service_id    = data.oci_core_services.regional_services.services.0.id
  }
}
#Creates a Service Gateway for the transit-hub Virtual Cloud Network
resource "oci_core_service_gateway" "hub_sgw" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  display_name    = "${var.hub_name} sgw"
  vcn_id          = oci_core_vcn.hub_vcn.id
  services {
    service_id    = data.oci_core_services.regional_services.services.0.id
  }
}

##Local Peering Gateways
#Creates a Local Peering Gateway in the shared-resources VCN to transit-hub
resource "oci_core_local_peering_gateway" "pod_hub_lpg" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  vcn_id          = oci_core_vcn.pod_vcn.id
  display_name    = "${var.pod_name} to transit-hub lpgw"
}
#Creates a Local Peering Gateway in the Transit Hub VCN to shared-resources
resource "oci_core_local_peering_gateway" "hub_pod_lpg" {
  compartment_id  = oci_identity_compartment.pod_compartment.id
  vcn_id          = oci_core_vcn.hub_vcn.id
  display_name    = "transit-hub to ${var.pod_name} lpgw"
  peer_id         = oci_core_local_peering_gateway.pod_hub_lpg.id
}

##Remote Peering Connections
#Creates a remote Peering Connection in the transit-hub
resource "oci_core_remote_peering_connection" "hub_rpcs" {
  for_each          = var.remote_peers
  compartment_id    = oci_identity_compartment.pod_compartment.id
  drg_id            = oci_core_drg.hub_drg.id
  display_name      = "transit-hub to ${each.key} rpc"
  peer_id           = each.value
  peer_region_name  = each.key
}

##Internet Gateways
#Creates an Internet Gateway for Shared Resources
resource "oci_core_internet_gateway" "pod_igw" {
  compartment_id = oci_identity_compartment.pod_compartment.id
  display_name   = "${var.pod_name} igw"
  vcn_id         = oci_core_vcn.pod_vcn.id
}
#Creates a NAT Gateway for Shared Resources
resource "oci_core_nat_gateway" "pod_natgw01" {
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${var.pod_name} natgw 01"
  block_traffic  = "false"
}
