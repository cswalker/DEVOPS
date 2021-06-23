###Routing
##Route Rules must be removed before the associated gateway is removed
#Creates a routetable for each shared-resource private subnets
resource "oci_core_route_table" "privnet_routetables" {
  for_each            = var.oci_privnets
  compartment_id      = oci_identity_compartment.pod_compartment.id
  vcn_id              = oci_core_vcn.pod_vcn.id
  display_name        = "${each.key} routetable"
  #Route Rule for "Catch-all" to NAT Gateway
  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_nat_gateway.pod_natgw01.id
  }
  #Route Rule for "ON-PREM" to Transit Hub
  dynamic "route_rules" {
    for_each            = concat(var.onprem_nets, var.remote_nets, var.vpn_partners)
    iterator            = my_cidr
    content {
      destination       = my_cidr.value
      destination_type  = "CIDR_BLOCK"
      network_entity_id = oci_core_local_peering_gateway.pod_hub_lpg.id
    }
  }
  #Route Rule from Shared-Resources to dev
  route_rules {
    destination       = "10.12.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.dev.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to qa
  route_rules {
    destination       = "10.13.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.qa.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to uat
  route_rules {
    destination       = "10.14.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.uat.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to prod
  route_rules {
    destination       = "10.15.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.prod.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to corp
  route_rules {
    destination       = "10.16.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.corp.outputs.sr_pod_lpgw_id
  }
}

#creates a routetable for each shared-resource public subnets
resource "oci_core_route_table" "pubnet_routetables" {
  for_each            = var.oci_pubnets
  compartment_id      = oci_identity_compartment.pod_compartment.id
  vcn_id              = oci_core_vcn.pod_vcn.id
  display_name        = "${each.key} routetable"
  #Route Rule for "Catch-all" Internet Gateway
  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.pod_igw.id
  }
  #Route Rule for "ON-PREM" to Transit Hub
  dynamic "route_rules" {
    for_each            = concat(var.onprem_nets, var.remote_nets)
    iterator            = my_cidr
    content {
      destination       = my_cidr.value
      destination_type  = "CIDR_BLOCK"
      network_entity_id = oci_core_local_peering_gateway.pod_hub_lpg.id
    }
  }
  #Route Rule from Shared-Resources to dev
  route_rules {
    destination       = "10.12.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.dev.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to qa
  route_rules {
    destination       = "10.13.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.qa.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to uat
  route_rules {
    destination       = "10.14.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.uat.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to prod
  route_rules {
    destination       = "10.15.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.prod.outputs.sr_pod_lpgw_id
  #Route Rule from Shared-Resources to corp
  route_rules {
    destination       = "10.16.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.corp.outputs.sr_pod_lpgw_id
  }

  }
}

#Creates a Local Peering Gateway Route Table for the Transit-Hub
resource "oci_core_route_table" "hub_lpgw_routetable" {
  compartment_id        = oci_identity_compartment.pod_compartment.id
  vcn_id                = oci_core_vcn.hub_vcn.id
  display_name          = "transit-hub lpgw routetable"
  dynamic "route_rules" {
    for_each            = concat(var.onprem_nets, var.remote_nets, var.vpn_partners)
    iterator            = my_cidr
    content {
      destination       = my_cidr.value
      destination_type  = "CIDR_BLOCK"
      network_entity_id = oci_core_drg.hub_drg.id
    }
  }
}

#Creates a Service Gateway Route Table for the Transit-Hub
resource "oci_core_route_table" "hub_sgw_routetable" {
  compartment_id        = oci_identity_compartment.pod_compartment.id
  vcn_id                = oci_core_vcn.hub_vcn.id
  display_name          = "transit-hub sgw routetable"
  dynamic "route_rules" {
    for_each            = var.onprem_nets
    iterator            = my_cidr
    content {
      destination = my_cidr.value
      destination_type  = "CIDR_BLOCK"
      network_entity_id = oci_core_drg.hub_drg.id
    }
  }
}

#Creates a Dynamic Routing Gateway Route Table for Transit-Hub
resource "oci_core_route_table" "hub_drg_routetable" {
  compartment_id        = oci_identity_compartment.pod_compartment.id
  vcn_id                = oci_core_vcn.hub_vcn.id
  display_name          = "transit-hub drg routetable"
  #Route Rule for Shared-Resources
  route_rules {
    destination         = "10.10.0.0/16"
    destination_type    = "CIDR_BLOCK"
    network_entity_id   = oci_core_local_peering_gateway.hub_pod_lpg.id
  }
  #Route Rule for dev
  route_rules {
    destination       = "10.12.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.dev.outputs.hub_pod_lpgw_id
  }
    #Route Rule from Shared-Resources to qa
  route_rules {
    destination       = "10.13.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.qa.outputs.sr_pod_lpgw_id
  }
  #Route Rule from Shared-Resources to uat
  route_rules {
    destination       = "10.14.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.uat.outputs.sr_pod_lpgw_id
  }
    #Route Rule from Shared-Resources to prod
  route_rules {
    destination       = "10.15.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.prod.outputs.sr_pod_lpgw_id
  }
    #Route Rule from transit-hub to corp
  route_rules {
    destination       = "10.16.0.0/16"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = data.terraform_remote_state.corp.outputs.hub_pod_lpgw_id
  }
  #Route Rule for Transit-Hub Service Gateway
  route_rules {
    destination         = lookup(data.oci_core_services.regional_services.services[0], "cidr_block")
    destination_type    = "SERVICE_CIDR_BLOCK"
    network_entity_id   = oci_core_service_gateway.hub_sgw.id
  }
}

#Attaches the Dynamic Routing Gateway to the transit-hub DRG
resource "oci_core_drg_attachment" "hub_drg_attach" {
  drg_id          = oci_core_drg.hub_drg.id
  vcn_id          = oci_core_vcn.hub_vcn.id
  route_table_id  = oci_core_route_table.hub_drg_routetable.id
  display_name    = "transit-hub_drg_attachment"
}
