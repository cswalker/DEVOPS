#Tag namespace for `dev`.
resource "oci_identity_tag_namespace" "tag_namespace_dev" {
    compartment_id = oci_identity_compartment.pod_compartment.id
    description = "Dev tag namespace."
    name = "dev"
}
