#Tag namespace for `qa`.
resource "oci_identity_tag_namespace" "tag_namespace_qa" {
    compartment_id = oci_identity_compartment.pod_compartment.id
    description = "QA tag namespace."
    name = "qa"
}
