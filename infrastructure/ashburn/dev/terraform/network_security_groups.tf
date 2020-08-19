### Network Security Groups
resource "oci_core_network_security_group" "pod_nsgs" {
  for_each       = merge(var.oci_privnets, var.oci_pubnets)
  compartment_id = oci_identity_compartment.pod_compartment.id
  vcn_id         = oci_core_vcn.pod_vcn.id
  display_name   = "${each.key} nsg"
}
