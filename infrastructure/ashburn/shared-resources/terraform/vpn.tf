###VPN IPSEC Tunnels
##Customer Premise Equipment
#Creates the Customer Premise Equipment in OCI
resource "oci_core_cpe" "vpn_cpe" {
    compartment_id  = oci_identity_compartment.pod_compartment.id
    ip_address      = var.cpe_ip
    display_name    = "ON-PREM Datacenter"
}

##IPSec Connections
#Creates the ON-PREM to OCI VPN IPSec Connection
resource "oci_core_ipsec" "vpn_ipsec" {
    compartment_id  = oci_identity_compartment.pod_compartment.id
    cpe_id          = oci_core_cpe.vpn_cpe.id
    drg_id          = oci_core_drg.hub_drg.id
    static_routes   = var.static_routes
    display_name    = "on-prem ipsec connection"
}
#Attaches the ON-PREM VPN IPSec connection to IPSec Tunnels
data "oci_core_ipsec_connection_tunnels" "ipsec_tunnels" {
    ipsec_id = oci_core_ipsec.vpn_ipsec.id
}

##Management Interfaces
#Creates ON-PREM VPN IPsec Tunnel Management Interface 1
resource "oci_core_ipsec_connection_tunnel_management" "ipsec_mgt_tun1" {
    ipsec_id                    = oci_core_ipsec.vpn_ipsec.id
    tunnel_id                   = data.oci_core_ipsec_connection_tunnels.ipsec_tunnels.ip_sec_connection_tunnels.0.id
    routing                     = "BGP"
    bgp_session_info {
        customer_bgp_asn        = var.customer_bgp_asn
        customer_interface_ip   = var.customer_interface_ip1
        oracle_interface_ip     = var.oracle_interface_ip1
    }
}
#Creates ON-PREM VPN IPSec Tunnel Management Interface 2
resource "oci_core_ipsec_connection_tunnel_management" "ipsec_mgt_tun2" {
    ipsec_id                    = oci_core_ipsec.vpn_ipsec.id
    tunnel_id                   = data.oci_core_ipsec_connection_tunnels.ipsec_tunnels.ip_sec_connection_tunnels.1.id
    routing                     = "BGP"
    bgp_session_info {
        customer_bgp_asn        = var.customer_bgp_asn
        customer_interface_ip   = var.customer_interface_ip2
        oracle_interface_ip     = var.oracle_interface_ip2
    }
}
