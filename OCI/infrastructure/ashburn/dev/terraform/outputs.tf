#VCN ID Output
output "pod_vcn_id" {
    value = oci_core_vcn.pod_vcn.id
}

#Transit Hub to compartment LPGW ID Output
output "hub_pod_lpgw_id" {
    value = oci_core_local_peering_gateway.hub_pod_lpg.id
}

#Shared-Resources to compartment LPGW ID Output
output "sr_pod_lpgw_id" {
    value = oci_core_local_peering_gateway.sr_pod_lpg.id
}

#Corp to Compartment LPGW ID Output
output "corp_pod_lpgw_id" {
    value = oci_core_local_peering_gateway.corp_pod_lpg.id
}
