##
## User Policies
##

# Policy for developers.
resource "oci_identity_policy" "developers_policy" {
    compartment_id = var.tenancy_ocid
    name = "Developers"
    description = "Developer group permissions."

    statements = [
        "Allow group Developers to manage all-resources in compartment dev",
        "Allow group Developers to manage all-resources in compartment qa",
        "Allow group Developers to manage all-resources in compartment uat",
        "Allow group Developers to read all-resources in compartment shared-resources",
        "Allow group Developers to read all-resources in compartment switchfly-infra-storage"
    ]
}

# Policy for Production Operations.
resource "oci_identity_policy" "operations_policy" {
    compartment_id = var.tenancy_ocid
    name = "Operations"
    description = "Production Operations group permissions."

    statements = [
        "Allow group Developers to manage all-resources in compartment prod",
        "Allow group Developers to read all-resources in compartment shared-resources",
        "Allow group Developers to read all-resources in compartment switchfly-infra-storage"
    ]
}

##
## Resource Policies
##

#Policy for managing resources in the shared-resources compartment.
resource "oci_identity_policy" "policy_shared_resources_manage" {
    compartment_id = var.tenancy_ocid
    name = "SharedResources_Manage"
    description = "Policy for managing resources in the shared-resources compartment."

    statements = [
        "Allow dynamic-group ProdManager to manage all-resources in compartment shared-resources"
    ]

    depends_on = [oci_identity_dynamic_group.dynamic_group_prod_manager]
}

#Policy for read/write access to resources in the shared-resources compartment.
resource "oci_identity_policy" "policy_shared_resources_read_write" {
    compartment_id = var.tenancy_ocid
    name = "SharedResources_ReadWrite"
    description = "Policy for read/write access to resources in the shared-resources compartment."

    #TODO: Add restrictions to prevent deletion of critical resources.
    statements = [
        <<EOF
        Allow dynamic-group PreProdManager
        to manage all-resources
        in compartment shared-resources
        EOF
    ]

    depends_on = [oci_identity_dynamic_group.dynamic_group_preprod_manager]
}

#Policy for read/write access to resources in the infra-storage compartment.
resource "oci_identity_policy" "policy_infra_storage_read_write" {
    compartment_id = var.tenancy_ocid
    name = "InfraStorage_ReadWrite"
    description = "Policy for read/write access to resources in the infra-storage compartment."

    #TODO: Add restrictions to prevent deletion of critical resources.
    statements = [
        <<EOF
        Allow dynamic-group ProdManager, PreProdManager
        to manage all-resources
        in compartment switchfly-infra-storage
        EOF
    ]

    depends_on = [
        oci_identity_dynamic_group.dynamic_group_preprod_manager,
        oci_identity_dynamic_group.dynamic_group_prod_manager
    ]
}

#Policy for managing resources in the dev compartment.
resource "oci_identity_policy" "policy_dev_manage" {
    compartment_id = var.tenancy_ocid
    name = "Dev_Manage"
    description = "Policy for managing resources in the dev compartment."

    statements = [
        "Allow dynamic-group DevManager to manage all-resources in compartment dev"
    ]

    depends_on = [oci_identity_dynamic_group.dynamic_group_dev_manager]
}

#Policy for managing resources in the qa compartment.
resource "oci_identity_policy" "policy_qa_manage" {
    compartment_id = var.tenancy_ocid
    name = "QA_Manage"
    description = "Policy for managing resources in the qa compartment."

    statements = [
        "Allow dynamic-group QAManager to manage all-resources in compartment qa"
    ]

    depends_on = [oci_identity_dynamic_group.dynamic_group_qa_manager]
}

#Policy for managing resources in the uat compartment.
resource "oci_identity_policy" "policy_uat_manage" {
    compartment_id = var.tenancy_ocid
    name = "UAT_Manage"
    description = "Policy for managing resources in the uat compartment."

    statements = [
        "Allow dynamic-group UATManager to manage all-resources in compartment uat"
    ]

    depends_on = [oci_identity_dynamic_group.dynamic_group_uat_manager]
}

#Policy for managing resources in the prod compartment.
resource "oci_identity_policy" "policy_prod_manage" {
    compartment_id = var.tenancy_ocid
    name = "Prod_Manage"
    description = "Policy for managing resources in the prod compartment."

    statements = [
        "Allow dynamic-group ProdManager to manage all-resources in compartment prod"
    ]

    depends_on = [oci_identity_dynamic_group.dynamic_group_prod_manager]
}

#Policy for managing resources in the corp compartment.
resource "oci_identity_policy" "policy_corp_manage" {
  compartment_id = var.tenancy_ocid
  name           = "Corp_Manage"
  description    = "Policy for managing resources in the corp compartment."

  statements = [
    "Allow dynamic-group CorpManager to manage all-resources in compartment corp"
  ]

  depends_on = [oci_identity_dynamic_group.dynamic_group_corp_manager]
}
