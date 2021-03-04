#Tag namespace for `uat`.
resource "oci_identity_tag_namespace" "tag_namespace_uat" {
    compartment_id = oci_identity_compartment.pod_compartment.id
    description = "UAT tag namespace."
    name = "uat"
}
