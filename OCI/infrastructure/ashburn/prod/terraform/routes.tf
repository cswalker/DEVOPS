###Routing
#Creates a routetable for each private subnet in Compartment
resource "oci_core_route_table" "privnet_routetables" {
  for_each       = var.oci_privnets
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${each.key} routetable"
  #Catch all Route Rules
  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_nat_gateway.pod_natgw01.id
  }
  #Route Rule from subnet to shared-resources
  route_rules {
    destination       = "10.10.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_local_peering_gateway.pod_sr_lpg.id
  }
    #Route Rule from subnet to corp
  route_rules {
    destination       = "10.16.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_local_peering_gateway.pod_corp_lpg.id
  }
  #Route Rule from Subnet to Transit Hub
  dynamic "route_rules" {
    for_each = concat(var.onprem_nets, var.remote_nets, var.vpn_partners)
    iterator = my_cidr
    content {
      destination       = my_cidr.value
      destination_type  = "CIDR_BLOCK"
      network_entity_id = oci_core_local_peering_gateway.pod_hub_lpg.id
    }
  }
}
#creates a routetable for each public subnet
resource "oci_core_route_table" "pubnet_routetables" {
  for_each       = var.oci_pubnets
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${each.key} routetable"
  #Catch-all Route Rules
  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.pod_igw.id
  }
  #Route Rule from subnet to shared-resources
  route_rules {
    destination       = "10.10.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_local_peering_gateway.pod_sr_lpg.id
  }
    #Route Rule from subnet to corp
  route_rules {
    destination       = "10.16.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_local_peering_gateway.pod_corp_lpg.id
  }
  #Route Rule from subnet to Transit Hub
  dynamic "route_rules" {
    for_each = concat(var.onprem_nets, var.remote_nets)
    iterator = my_cidr
    content {
      destination       = my_cidr.value
      destination_type  = "CIDR_BLOCK"
      network_entity_id = oci_core_local_peering_gateway.pod_hub_lpg.id
    }
  }
}
