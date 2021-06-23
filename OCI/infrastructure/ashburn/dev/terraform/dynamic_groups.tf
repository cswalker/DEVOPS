#Dynamic group for managing dev keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "dev_dynamic_group_manage_keys" {
    compartment_id = var.tenancy_ocid
    description    = "For managing dev keys in the shared-resources vault."
    matching_rule  = "All {tag.dev.ManageKeys.value = 'true'}"
    name           = "Vault_Dev_ManageKeys"
}

#Dynamic group for managing dev secrets in the shared-resources vault.
resource "oci_identity_dynamic_group" "dev_dynamic_group_manage_secrets" {
    compartment_id = var.tenancy_ocid
    description    = "For managing dev secrets in the shared-resources vault."
    matching_rule  = "All {tag.dev.ManageSecrets.value = 'true'}"
    name           = "Vault_Dev_ManageSecrets"
}
