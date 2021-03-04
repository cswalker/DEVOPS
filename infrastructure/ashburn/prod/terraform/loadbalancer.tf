# Public load balancer #1 for prod environment.
resource "oci_load_balancer_load_balancer" "loadbalancer_prod_public_1" {
    compartment_id = local.oci_vars.compartments.prod.compartment_ocid
    display_name = "Prod Public Load Balancer 1"
    ip_mode = "IPV4"
    is_private = false
    shape = "400Mbps"
    subnet_ids = [local.oci_vars.regions.ashburn.prod.vcn.subnets.pub_ocid]

    defined_tags = {
        "Common.EnvironmentType" = "prod",
        "LoadBalancers.Visibility" = "public"
    }
}

# Public load balancer #2 for prod environment.
resource "oci_load_balancer_load_balancer" "loadbalancer_prod_public_2" {
    compartment_id = local.oci_vars.compartments.prod.compartment_ocid
    display_name = "Prod Public Load Balancer 2"
    ip_mode = "IPV4"
    is_private = false
    shape = "400Mbps"
    subnet_ids = [local.oci_vars.regions.ashburn.prod.vcn.subnets.pub_ocid]

    defined_tags = {
        "Common.EnvironmentType" = "prod",
        "LoadBalancers.Visibility" = "public"
    }
}

# Public load balancer #3 for prod environment.
resource "oci_load_balancer_load_balancer" "loadbalancer_prod_public_3" {
    compartment_id = local.oci_vars.compartments.prod.compartment_ocid
    display_name = "Prod Public Load Balancer 3"
    ip_mode = "IPV4"
    is_private = false
    shape = "400Mbps"
    subnet_ids = [local.oci_vars.regions.ashburn.prod.vcn.subnets.pub_ocid]

    defined_tags = {
        "Common.EnvironmentType" = "prod",
        "LoadBalancers.Visibility" = "public"
    }
}
