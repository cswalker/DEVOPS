tenancy_ocid            = "Insert your OCI Tenancy OCID"
pod_region              = "us-ashburn-1" #Replace with you OCI Region
pod_name                = "dev"
pod_cidr                = "10.12.0.0/16" #Insert your desired Spoke CIDR
pod_dns_label           = "dev"
pod_custom_dns_servers  = ["10.12.1.2", "10.12.1.3"] #Insert a string list of your preferred custom dns servers
pod_search_domain_names = ["dev.custom.dns.my.com"] #Insert a custom dns domain for
hub_compartment_id      = "" #Shared-Resources Compartment OCID
