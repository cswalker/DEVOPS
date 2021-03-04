#Dynamic groups for managing UAT keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "uat_dynamic_group_manage_keys" {
    compartment_id = var.tenancy_ocid
    description    = "For managing UAT keys in the shared-resources vault."
    matching_rule  = "All {tag.uat.ManageKeys.value = 'true'}"
    name           = "Vault_UAT_ManageKeys"
}

#Dynamic groups for managing UAT keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "uat_dynamic_group_manage_secrets" {
    compartment_id = var.tenancy_ocid
    description    = "For managing UAT secrets in the shared-resources vault."
    matching_rule  = "All {tag.uat.ManageSecrets.value = 'true'}"
    name           = "Vault_UAT_ManageSecrets"
}
