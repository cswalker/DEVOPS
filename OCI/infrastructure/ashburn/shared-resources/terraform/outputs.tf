output "pod_vcn_id" {
    value = oci_core_vcn.pod_vcn.id
}

output "hub_vcn_id" {
    value = oci_core_vcn.hub_vcn.id
}

output "hub_lpgw_routetable_id" {
    value = oci_core_route_table.hub_lpgw_routetable.id
}