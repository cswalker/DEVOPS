#Dynamic group for managing prod keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "prod_dynamic_group_manage_keys" {
    compartment_id = var.tenancy_ocid
    description    = "For managing prod keys in the shared-resources vault."
    matching_rule  = "All {tag.prod.ManageKeys.value = 'true'}"
    name           = "Vault_Prod_ManageKeys"
}

#Dynamic group for managing prod secrets in the shared-resources vault.
resource "oci_identity_dynamic_group" "prod_dynamic_group_manage_secrets" {
    compartment_id = var.tenancy_ocid
    description    = "For managing prod secrets in the shared-resources vault."
    matching_rule  = "All {tag.prod.ManageSecrets.value = 'true'}"
    name           = "Vault_Prod_ManageSecrets"
}
