#Dynamic group for managing Corp keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "corp_dynamic_group_manage_keys" {
    compartment_id = var.tenancy_ocid
    description    = "For managing Corp keys in the shared-resources vault."
    matching_rule  = "All {tag.corp.ManageKeys.value = 'true'}"
    name           = "Vault_Corp_ManageKeys"
}

#Dynamic group for managing Corp secrets in the shared-resources vault.
resource "oci_identity_dynamic_group" "corp_dynamic_group_manage_secrets" {
    compartment_id = var.tenancy_ocid
    description    = "For managing corp secrets in the shared-resources vault."
    matching_rule  = "All {tag.corp.ManageSecrets.value = 'true'}"
    name           = "Vault_Corp_ManageSecrets"
}
