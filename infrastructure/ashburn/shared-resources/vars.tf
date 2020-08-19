#POD Configuration
variable "tenancy_ocid" {}
variable "pod_region" {}
variable "pod_name" {}
variable "hub_name" {}
variable "pod_cidr" {}
variable "hub_cidr" {}
variable "pod_dns_label" {}
variable "hub_dns_label" {}
variable "pod_custom_dns_servers" {}
variable "pod_search_domain_names" {}

#On-Prem IPSEC Configuration
variable "cpe_ip" {}
variable "customer_bgp_asn" {}
variable "customer_interface_ip1" {}
variable "oracle_interface_ip1" {}
variable "customer_interface_ip2" {}
variable "oracle_interface_ip2" {}
variable "static_routes" {
  type    = list(string)
  default = ["10.10.0.0/16", "192.168.10.0/21"] #Insert a string list of on-prem CIDRs
}

variable "onprem_nets" {
  type        = list(string)
  description = "On-Prem Network CIDRs to be added to the pod's route table"
  default     = []
}

variable "remote_peers" {
  type        = map(any)
  description = "Remote CIDRs which will be routed via RPC to other OCI regions"
  default     = {}
}

variable "remote_nets" {
  type        = list(string)
  description = "Remote CIDRs which will be routed via RPC to other OCI regions"
  default     = []
}

variable "oci_nets" {
  type        = list(string)
  description = "Neighboring OCI Virtual Cloud Network CIDRs"
  default     = []
}

variable "oci_privnets" {
  type = map(string)
  description = "Subnet CIDRs"
  default = {
    "private_subnet1"  = "10.10.1.0/24"
    }
}

variable "oci_pubnets" {
  type = map(string)
  description = "Subnet CIDRs"
  default = {
    "public_subnet1"   = "10.10.2.0/24"
  }
}
