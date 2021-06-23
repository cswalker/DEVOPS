#Dynamic group for managing QA keys in the shared-resources vault.
resource "oci_identity_dynamic_group" "qa_dynamic_group_manage_keys" {
    compartment_id = var.tenancy_ocid
    description    = "For managing QA keys in the shared-resources vault."
    matching_rule  = "All {tag.qa.ManageKeys.value = 'true'}"
    name           = "Vault_QA_ManageKeys"
}

#Dynamic group for managing QA secrets in the shared-resources vault.
resource "oci_identity_dynamic_group" "qa_dynamic_group_manage_secrets" {
    compartment_id = var.tenancy_ocid
    description    = "For managing QA secrets in the shared-resources vault."
    matching_rule  = "All {tag.qa.ManageSecrets.value = 'true'}"
    name           = "Vault_QA_ManageSecrets"
}
