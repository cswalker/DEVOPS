#POD Configuration
variable "tenancy_ocid" {}
variable "pod_region" {}
variable "pod_name" {}
variable "pod_cidr" {}
variable "pod_dns_label" {}
variable "pod_custom_dns_servers" {}
variable "pod_search_domain_names" {}
variable "jenkins_cidr" {}
variable "corp_cidr" {}
variable "hub_compartment_id" {}

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
  default     = ["10.12.0.0/16"]
}

variable "oci_privnets" {
  type = map(string)
  description = "Subnet CIDRs"
  default = {
    "svc"  = "10.16.1.0/24"
    }
}

variable "oci_pubnets" {
  type = map(string)
  description = "Subnet CIDRs"
  default = {
    "public_subnet1"   = "10.16.2.0/24"
  }
}
