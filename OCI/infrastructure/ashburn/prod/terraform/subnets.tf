###Subnetting
#Creates the security list that spans all subnets in the Virtual Cloud Network
resource "oci_core_security_list" "pod_vcnwide_seclist" {
  compartment_id = oci_identity_compartment.pod_compartment.id
  display_name   = "${var.pod_name} vcn-wide seclist"
  vcn_id         = oci_core_vcn.pod_vcn.id
  egress_security_rules {
    destination = "0.0.0.0/0"
    protocol    = "all"
  }
  #SSH
  dynamic "ingress_security_rules" {
    for_each = concat(var.onprem_nets, var.remote_nets, var.oci_nets, values(var.oci_privnets), values(var.oci_pubnets), [var.pod_cidr])
    iterator = my_cidr
    content {
      description = "SSH"
      source   = my_cidr.value
      protocol = "6"
      tcp_options {
        min = 22
        max = 22
      }
    }
  }
  #ICMP
  dynamic "ingress_security_rules" {
    for_each = concat(var.onprem_nets, var.remote_nets, values(var.oci_privnets), values(var.oci_pubnets), [var.pod_cidr])
    iterator = my_cidr
    content {
      description = "ICMP"
      source   = my_cidr.value
      protocol = "1"
    }
  }
}

#Creates the Security list for each subnet
resource "oci_core_security_list" "subnet_seclists" {
  for_each       = merge(var.oci_privnets, var.oci_pubnets)
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${each.key} seclist"
 # allow all traffic within a subnet
  ingress_security_rules {
    description = "Allow all traffic from self"
    protocol    = "all"
    source      = each.value
    source_type = "CIDR_BLOCK"
  }
}

#Create each Private Subnet
resource "oci_core_subnet" "private_subnets" {
  for_each                   = var.oci_privnets
  cidr_block                 = each.value
  display_name               = "${each.key} subnet"
  compartment_id             = oci_identity_compartment.pod_compartment.id
  vcn_id                     = oci_core_vcn.pod_vcn.id
  prohibit_public_ip_on_vnic = true
  route_table_id             = oci_core_route_table.privnet_routetables[each.key].id
  security_list_ids          = [oci_core_security_list.pod_vcnwide_seclist.id, oci_core_security_list.subnet_seclists[each.key].id]
  dhcp_options_id            = oci_core_dhcp_options.pod_dhcp_options[each.key].id
  dns_label                  = each.key
}

#Create each Public Subnet
resource "oci_core_subnet" "public_subnets" {
  for_each          = var.oci_pubnets
  cidr_block        = each.value
  display_name      = "${each.key} subnet"
  compartment_id    = oci_identity_compartment.pod_compartment.id
  vcn_id            = oci_core_vcn.pod_vcn.id
  route_table_id    = oci_core_route_table.pubnet_routetables[each.key].id
  security_list_ids = [oci_core_security_list.pod_vcnwide_seclist.id, oci_core_security_list.subnet_seclists[each.key].id]
  dhcp_options_id   = oci_core_dhcp_options.pod_dhcp_options[each.key].id
  dns_label         = each.key
}
