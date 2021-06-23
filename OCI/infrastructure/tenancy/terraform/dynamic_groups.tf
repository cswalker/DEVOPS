#Dynamic group for managing the dev compartment.
resource "oci_identity_dynamic_group" "dynamic_group_dev_manager" {
    compartment_id = var.tenancy_ocid
    name           = "DevManager"
    description    = "Dynamic group for managing the dev compartment."
    matching_rule  = <<EOF
        Any {
            tag.AccessControl.EnvManager.value = 'dev',
            tag.AccessControl.EnvManager.value = 'preprod'
        }
    EOF
}

#Dynamic group for managing the qa compartment.
resource "oci_identity_dynamic_group" "dynamic_group_qa_manager" {
    compartment_id = var.tenancy_ocid
    name           = "QAManager"
    description    = "Dynamic group for managing the qa compartment."
    matching_rule  = <<EOF
        Any {
            tag.AccessControl.EnvManager.value = 'qa',
            tag.AccessControl.EnvManager.value = 'preprod'
        }
    EOF
}

#Dynamic group for managing the uat compartment.
resource "oci_identity_dynamic_group" "dynamic_group_uat_manager" {
    compartment_id = var.tenancy_ocid
    name           = "UATManager"
    description    = "Dynamic group for managing the uat compartment."
    matching_rule  = <<EOF
        Any {
            tag.AccessControl.EnvManager.value = 'uat',
            tag.AccessControl.EnvManager.value = 'preprod'
        }
    EOF
}

#Dynamic group for managing the prod compartment.
resource "oci_identity_dynamic_group" "dynamic_group_prod_manager" {
    compartment_id = var.tenancy_ocid
    name           = "ProdManager"
    description    = "Dynamic group for managing the prod compartment."
    matching_rule  = "tag.AccessControl.EnvManager.value = 'prod'"
}

#Dynamic group for managing the corp compartment.
resource "oci_identity_dynamic_group" "dynamic_group_corp_manager" {
    compartment_id = var.tenancy_ocid
    name           = "CorpManager"
    description    = "Dynamic group for managing the corp compartment."
    matching_rule  = "tag.AccessControl.EnvManager.value = 'corp'"
}

#Dynamic group for managing the shared-resources and infra-storage compartments.
resource "oci_identity_dynamic_group" "dynamic_group_preprod_manager" {
    compartment_id = var.tenancy_ocid
    name           = "PreProdManager"
    description    = "Dynamic group for managing the shared-resources and infra-storage compartments."
    matching_rule  = <<EOF
        Any {
            tag.AccessControl.EnvManager.value = 'dev',
            tag.AccessControl.EnvManager.value = 'qa',
            tag.AccessControl.EnvManager.value = 'uat',
            tag.AccessControl.EnvManager.value = 'preprod'
        }
    EOF
}
