tenancy_ocid            = "Insert your OCI Tenancy OCID"
pod_region              = "us-ashburn-1" #Replace with you OCI Region
pod_name                = "qa"
pod_cidr                = "10.13.0.0/16"
pod_dns_label           = "qa"
pod_custom_dns_servers  = ["10.13.1.2", "10.13.1.3"]
pod_search_domain_names = ["qa.custom.dns.my.com"] #Insert a custom dns domain for
hub_compartment_id      = "" #Shared-Resources Compartment OCID
