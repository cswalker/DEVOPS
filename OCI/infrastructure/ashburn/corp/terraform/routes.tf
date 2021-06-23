###Routing
#Creates a routetable for each private subnet in Compartment
resource "oci_core_route_table" "privnet_routetables" {
  for_each            = var.oci_privnets
  compartment_id      = oci_identity_compartment.pod_compartment.id
  vcn_id              = oci_core_vcn.pod_vcn.id
  display_name        = "${each.key} routetable"
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
  #Route Rule from corp to dev
  route_rules {
    destination       = "10.12.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.dev.outputs.corp_pod_lpgw_id
  }
  #Route Rule from corp to qa
  route_rules {
    destination       = "10.13.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.qa.outputs.corp_pod_lpgw_id
  }
    #Route Rule from corp to prod
  route_rules {
    destination       = "10.15.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.prod.outputs.corp_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to uat
  route_rules {
    destination       = "10.14.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.uat.outputs.corp_pod_lpgw_id
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
  #Route Rule from Corp to dev
  route_rules {
    destination       = "10.12.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.dev.outputs.corp_pod_lpgw_id
  }
  #Route Rule from Corp to qa
  route_rules {
    destination       = "10.13.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.qa.outputs.corp_pod_lpgw_id
  }
  #Route Rule from Corp to prod
  route_rules {
    destination       = "10.15.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.prod.outputs.corp_pod_lpgw_id
  }
  #Route Rule from subnet to shared-resources
  route_rules {
    destination       = "10.10.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_local_peering_gateway.pod_sr_lpg.id
  }
  #Route Rule from Shared-Resources to uat
  route_rules {
    destination       = "10.14.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.uat.outputs.corp_pod_lpgw_id
  }
}
