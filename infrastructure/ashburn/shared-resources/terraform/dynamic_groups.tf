#Dynamic group for managing the shared-resources vault itself.
resource "oci_identity_dynamic_group" "shared_resources_dynamic_group_manage_vault" {
    compartment_id = var.tenancy_ocid
    description    = "For managing the shared-resources vault itself."
    matching_rule  = "All {tag.shared-resources.ManageVault.value = 'true'}"
    name           = "Vault_SharedResources_ManageVault"
}

#Dynamic group for managing keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "shared_resources_dynamic_group_manage_keys" {
    compartment_id = var.tenancy_ocid
    description    = "For managing keys in the shared-resources vault."
    matching_rule  = "All {tag.shared-resources.ManageKeys.value = 'true'}"
    name           = "Vault_SharedResources_ManageKeys"
}

#Dynamic group for managing secrets in the shared-resources vault.
resource "oci_identity_dynamic_group" "shared_resources_dynamic_group_manage_secrets" {
    compartment_id = var.tenancy_ocid
    description    = "For managing secrets in the shared-resources vault."
    matching_rule  = "All {tag.shared-resources.ManageSecrets.value = 'true'}"
    name           = "Vault_SharedResources_ManageSecrets"
}
