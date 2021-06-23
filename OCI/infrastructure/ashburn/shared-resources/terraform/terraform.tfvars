#POD Configuration
tenancy_ocid            = "Insert your OCI Tenancy OCID"
pod_region              = "us-ashburn-1" #Replace with you OCI Region
pod_name                = "shared-resources"
hub_name                = "transit-hub"
pod_cidr                = "10.10.0.0/16" #Insert your desired Spoke CIDR
hub_cidr                = "10.11.10.0/16" #Insert your desired Transit-Hub CIDR
pod_dns_label           = "shared-resources"
hub_dns_label           = "transit-hub"
pod_custom_dns_servers  = ["10.10.1.2", "10.10.1.3"] #Insert a string list of your preferred custom dns servers
pod_search_domain_names = ["custom.dns.my.com"] #Insert a custom dns domain for  
corp_cidr               = "10.16.33.0/24"

#OnPrem IPSEC Configuration
cpe_ip                  = "555.555.555.555" #Insert your on prem vpn public IP
customer_bgp_asn        = "55555" #Insert your on prem BGP ASN Number
customer_interface_ip1  = "169.254.66.1/30" 
oracle_interface_ip1    = "169.254.66.2/30"
customer_interface_ip2  = "169.254.66.10/30"
oracle_interface_ip2    = "169.254.66.20/30"