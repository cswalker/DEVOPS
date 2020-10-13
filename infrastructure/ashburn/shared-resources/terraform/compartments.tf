###Compartments
#Creates the Compartment to match the environment
resource "oci_identity_compartment" "pod_compartment" {
  compartment_id = var.tenancy_ocid
  description    = "${var.pod_name} compartment"
  name           = var.pod_name
}
#Sets the compartment to create instance components
data "oci_identity_compartments" "pod_compartment" {
  compartment_id = var.tenancy_ocid
  filter {
    name   = "name"
    values = [var.pod_name]
  }
}
