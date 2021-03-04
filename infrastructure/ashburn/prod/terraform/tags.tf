#Tag namespace for `prod`.
resource "oci_identity_tag_namespace" "tag_namespace_prod" {
    compartment_id = oci_identity_compartment.pod_compartment.id
    description = "Prod tag namespace."
    name = "prod"
}
