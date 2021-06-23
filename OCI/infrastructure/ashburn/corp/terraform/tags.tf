#Tag namespace for `corp`.
resource "oci_identity_tag_namespace" "tag_namespace_corp" {
    compartment_id = oci_identity_compartment.pod_compartment.id
    description = "Corp tag namespace."
    name = "corp"
}
