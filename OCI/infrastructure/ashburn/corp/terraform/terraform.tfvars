#POD Configuration
tenancy_ocid            = "Insert your OCI Tenancy OCID"
pod_region              = "us-ashburn-1" #Replace with you OCI Region
pod_name                = "corp"
pod_cidr                = "10.16.0.0/16"
pod_dns_label           = "corp"
pod_custom_dns_servers  = ["10.16.1.2", "10.16.1.3"]
pod_search_domain_names = ["corp.custom.dns.my.com"]
hub_compartment_id      =  "" #Shared-Resources Compartment OCID
