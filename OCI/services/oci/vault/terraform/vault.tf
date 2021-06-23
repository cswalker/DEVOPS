##Vaults
#Dev Vault
resource "oci_kms_vault" "dev_vault" {
    compartment_id = local.oci_vars["compartments"]["dev"]["compartment_ocid"]
    display_name   = "Dev Vault"
    vault_type     = "DEFAULT"
}
#QA Vault
resource "oci_kms_vault" "qa_vault" {
    compartment_id = local.oci_vars["compartments"]["qa"]["compartment_ocid"]
    display_name   = "QA Vault"
    vault_type     = "DEFAULT"
}
#UAT Vault
resource "oci_kms_vault" "uat_vault" {
    compartment_id = local.oci_vars["compartments"]["uat"]["compartment_ocid"]
    display_name   = "UAT Vault"
    vault_type     = "DEFAULT"
}
#Prod Vault
resource "oci_kms_vault" "prod_vault" {
    compartment_id = local.oci_vars["compartments"]["prod"]["compartment_ocid"]
    display_name   = "Prod Vault"
    vault_type     = "DEFAULT"
}
#Shared Resources Vault
resource "oci_kms_vault" "shared_resources_vault" {
    compartment_id = local.oci_vars["compartments"]["shared"]["compartment_ocid"]
    display_name   = "Shared Resources Vault"
    vault_type     = "DEFAULT"
}
#corp Vault
resource "oci_kms_vault" "corp_vault" {
    compartment_id = local.oci_vars["compartments"]["corp"]["compartment_ocid"]
    display_name   = "Shared Resources Vault"
    vault_type     = "DEFAULT"
}

##Master Keys
#Dev Master Key
resource "oci_kms_key" "dev_vault_master_key" {
    compartment_id = local.oci_vars["compartments"]["dev"]["compartment_ocid"]
    display_name   = "Dev Master Key"
    key_shape {
        algorithm = "AES"
        length    = 32
    }
    management_endpoint = oci_kms_vault.dev_vault.management_endpoint

    depends_on = [oci_kms_vault.dev_vault]
}
#QA Master Key
resource "oci_kms_key" "qa_vault_master_key" {
    compartment_id = local.oci_vars["compartments"]["qa"]["compartment_ocid"]
    display_name   = "QA Master Key"
    key_shape {
        algorithm = "AES"
        length    = 32
    }
    management_endpoint = oci_kms_vault.qa_vault.management_endpoint

    depends_on = [oci_kms_vault.qa_vault]
}
#UAT Master Key
resource "oci_kms_key" "uat_vault_master_key" {
    compartment_id = local.oci_vars["compartments"]["uat"]["compartment_ocid"]
    display_name   = "UAT Master Key"
    key_shape {
        algorithm = "AES"
        length    = 32
    }
    management_endpoint = oci_kms_vault.uat_vault.management_endpoint

    depends_on = [oci_kms_vault.uat_vault]
}
#Prod Master Key
resource "oci_kms_key" "prod_vault_master_key" {
    compartment_id = local.oci_vars["compartments"]["prod"]["compartment_ocid"]
    display_name   = "Prod Master Key"
    key_shape {
        algorithm = "AES"
        length    = 32
    }
    management_endpoint = oci_kms_vault.prod_vault.management_endpoint

    depends_on = [oci_kms_vault.prod_vault]
}
#Shared Resources Master Key
resource "oci_kms_key" "shared_resources_vault_master_key_shared_resources" {
    compartment_id = local.oci_vars["compartments"]["shared"]["compartment_ocid"]
    display_name   = "Shared Resources Master Key"
    key_shape {
        algorithm = "AES"
        length    = 32
    }
    management_endpoint = oci_kms_vault.shared_resources_vault.management_endpoint

    depends_on = [oci_kms_vault.shared_resources_vault]
}
#Corp Master Key
resource "oci_kms_key" "corp_vault_master_key_shared_resources" {
    compartment_id = local.oci_vars["compartments"]["corp"]["compartment_ocid"]
    display_name   = "Corp Master Key"
    key_shape {
        algorithm = "AES"
        length    = 32
    }
    management_endpoint = oci_kms_vault.corp_vault.management_endpoint

    depends_on = [oci_kms_vault.corp_vault]
}
