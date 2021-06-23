##
## AccessControl
##

# Tag namespace for managing access control across the tenancy.
resource "oci_identity_tag_namespace" "tag_namespace_access_control" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide tag namespace for access control."
    name = "AccessControl"
}

##
## AccessControl.EnvManager
##

# Tag for the environment types a resource can manage.
resource "oci_identity_tag" "access_control_environment_manager_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_access_control.id
    name = "EnvManager"
    description = "Tag for the environment types a resource can manage."

    validator {
        validator_type = "ENUM"
        values = [
            "dev",
            "qa",
            "uat",
            "prod",
            "corp",
            "preprod"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_access_control]
}


##
## Common
##

# Tag namespace for managing common tags across different resource types.
resource "oci_identity_tag_namespace" "tag_namespace_common" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide tag namespace for common tags."
    name = "Common"
}

##
## Common.ClientName
##

# Tag for the name of the client the resource is associated with.
resource "oci_identity_tag" "common_client_name_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_common.id
    name = "ClientName"
    description = "Name of the client the resource is associated with."

    validator {
        validator_type = "ENUM"
        values = [
            "qa",
            "uat",
            "training"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_common]
}

##
## Common.CreatedBy
##

# Tag for the environment that contains the resource.
resource "oci_identity_tag" "common_created_by_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_common.id
    name = "CreatedBy"
    description = "Creator of the resource."

    depends_on = [oci_identity_tag_namespace.tag_namespace_common]
}

##
## Common.EnvironmentName
##

# Tag for the resource's environment name.
resource "oci_identity_tag" "common_environment_name_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_common.id
    name = "EnvironmentName"
    description = "Tag for the resource's environment name."

    depends_on = [oci_identity_tag_namespace.tag_namespace_common]
}

##
## Common.EnvironmentType
##

# Tag for the environment that contains the resource.
resource "oci_identity_tag" "common_environment_type_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_common.id
    name = "EnvironmentType"
    description = "Tag for the environment that contains the resource."

    validator {
        validator_type = "ENUM"
        values = [
            "dev",
            "qa",
            "uat",
            "prod",
            "shared",
            "corp"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_common]
}

##
## Common.ReleaseVersion
##

# Tag for the release version associated with the resource.
resource "oci_identity_tag" "common_release_version_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_common.id
    name = "ReleaseVersion"
    description = "Release version associated with the resource."

    depends_on = [oci_identity_tag_namespace.tag_namespace_common]
}

##
## Databases
##

# Tag namespace for managing databases across the tenancy.
resource "oci_identity_tag_namespace" "tag_namespace_databases" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide namespace for database management."
    name = "Databases"
}

##
## Databases.DatabaseRole
##

# Tag for the role of a database instance.
resource "oci_identity_tag" "database_database_role_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_databases.id
    name = "DatabaseRole"
    description = "Tag for the role of a database instance."

    validator {
        validator_type = "ENUM"
        values = [
            "primary",
            "replica"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_databases]
}
##
## DNS
##

# Tag namespace for managing DNS servers across the tenancy.
resource "oci_identity_tag_namespace" "tag_namespace_dns" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide namespace for DNS server management."
    name = "DNS"
}

##
## DNS.DNSRole
##

# Tag for the role of a DNS instance.
resource "oci_identity_tag" "database_dns_role_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_dns.id
    name = "DNSRole"
    description = "Tag for the role of a DNS instance."

    validator {
        validator_type = "ENUM"
        values = [
            "primary",
            "secondary"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_dns]
}

##
## DomainController
##

# Tag namespace for managing Domain Controllers across the tenancy.
resource "oci_identity_tag_namespace" "tag_namespace_domaincontroller" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide namespace for Domain Controller management."
    name = "DomainController"
}

##
## DomainController.DomainControllerRole
##

# Tag for the role of a Domain Controller instance.
resource "oci_identity_tag" "database_domaincontroller_role_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_domaincontroller.id
    name = "DomainControllerRole"
    description = "Tag for the role of a Domain Controller instance."

    validator {
        validator_type = "ENUM"
        values = [
            "primary",
            "secondary"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_domaincontroller]
}


##
## Instances
##

# Tag namespace for managing instances across the tenancy.
resource "oci_identity_tag_namespace" "tag_namespace_instances" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide namespace for instance management."
    name = "Instances"
}

##
## Instances.ServerName
##

# Tag for the server name given to the instance.
resource "oci_identity_tag" "instance_server_name_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_instances.id
    name = "ServerName"
    description = "Tag for the server name given to the instance."

    depends_on = [oci_identity_tag_namespace.tag_namespace_instances]
}

##
## Instances.ServerType
##

# Tag for the type of server.
resource "oci_identity_tag" "instance_server_type_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_instances.id
    name = "ServerType"
    description = "Tag for the type of server."

    validator {
        validator_type = "ENUM"
        values = [
            "app",
            "build",
            "checkmk",
            "db",
            "dns",
            "domain-controller",
            "jenkins",
            "loadbalancer",
            "loadtest",
            "report",
            "sftp",
            "splunk",
            "static",
            "yum-mirror"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_instances]
}


##
## Load Balancers
##

# Tag namespace for managing load balancers across the tenancy.
resource "oci_identity_tag_namespace" "tag_namespace_loadbalancers" {
    compartment_id = var.tenancy_ocid
    description = "Tenancy-wide namespace for load balancer management."
    name = "LoadBalancers"
}

# Tag for the load balancer visibility.
resource "oci_identity_tag" "loadbalancer_visibility_tag" {
    tag_namespace_id = oci_identity_tag_namespace.tag_namespace_loadbalancers.id
    name = "Visibility"
    description = "Tag for the load balancer visibility."

    validator {
        validator_type = "ENUM"
        values = [
            "public",
            "private"
        ]
    }

    depends_on = [oci_identity_tag_namespace.tag_namespace_loadbalancers]
}
