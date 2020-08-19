###DHCP
#Creates the DHCP Options for each subnet in Shared-Resources
resource "oci_core_dhcp_options" "pod_dhcp_options" {
  for_each       = merge(var.oci_privnets, var.oci_pubnets)
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${each.key} dhcp opts"
  dynamic "options" {
    for_each = each.key != "svc" ? [each.key] : []
    iterator = item
    content {
      type               = "DomainNameServer"
      server_type        = "CustomDnsServer"
      custom_dns_servers = var.pod_custom_dns_servers
    }
  }
  dynamic "options" {
    for_each = each.key == "svc" ? [each.key] : []
    iterator = item
    content {
      type        = "DomainNameServer"
      server_type = "VcnLocalPlusInternet"
    }
  }
  options {
    type                = "SearchDomain"
    search_domain_names = var.pod_search_domain_names
  }
}
