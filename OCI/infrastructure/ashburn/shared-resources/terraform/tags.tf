#Tag namespace for `shared-resources`.
resource "oci_identity_tag_namespace" "tag_namespace_shared_resources" {
    compartment_id = oci_identity_compartment.pod_compartment.id
    description = "Shared Resources tag namespace."
    name = "shared-resources"
}
