Terraform Documentation

     https://www.terraform.io/docs/index.html

Oracle Terraform Documentation

     https://www.terraform.io/docs/providers/oci/index.html

Terraform Configurations: 

  /infrastructure


Terraform Devops

The Terraform Configuration is used to deploy core resources and environment type divisions within Oracle Cloud Infrastructure. The authentication type requires the code be initialized and applied from an instance principal within OCI itself. This instance principal here is currently Jenkins.


Directory Structure

Within the Devops Git Repository, all of the Terraform Code is found within the infrastructure directory further subdivided by region in which the resources exists. From the desired region, you will find a directory for each environment type. The structure is designed in a Hub and Spoke Methodology. All traffic routing and shared resources are handled within the shared-resources directory. Each other directory is an individual spoke that mirrors an environment type.


Hub Configuration

The hub resources can all be found within the shared-resources directory. This directory has the following files:

  >shared-resources

    >compartments.tf

      Handles creating and retrieving data related to the shared-resources compartments.

    >core_network.tf

      Handles creating and retrieving data related to core networking components. This file should almost never be modified.

    >dhcp.tf

      Handles creating and retrieving data related to DHCP options and configurations.

    >main.tf

      Handles the Initialization of Terraform with the OCI provider, sets the terraform state data storage location, and handles the importing of Oracle regional services

    >network_security_groups.tf

      Will Handle the creation and nesting of Network Security Groups

    >outputs.tf

      Used to create variables that other Terraform Configurations can use to retrieve information about the shared-resources compartment resources.

    >routing.tf

      Handles the creation of Route Tables and Route Rules.

    >subnetting.tf

      Handles the creation of Security Lists and Subnets.

    >terraform.tfvars

      This file is where we define all variables found within the terraform configuration files.

    >vars.tf

      This file is where we declare all variables found within the terraform configurations files.

    >vpn.tf

      This file handles the creation of VPN IPSEC tunnels to on premise data-centers. This file is unique and not found in any spoke configuration directory.


Spoke Configuration

Spoke resources can all be found within the corresponding environment directory. E.G Dev corresponds to all Dev Compartment Resources. These Directories have all the same files as the shared-resources directory excluding vpn.tf, and they behave in the same manner.


Creating a New Spoke

  Object Storage & Terraform State Data
    Navigate to OCI Control Panel, then to object storage.
    Create a new bucket. Bucket name should be [compartment_name]. E.g. Devlopment.
    Within the new bucket, upload a 0kb file named terraform.tfstate.

  Create a Pre-Authenticated Request for the new terraform.tfstate file.
    The name should be terraform.tfstate_par
    The target should be object
    The object should be terraform.tfstate
    Access type should be reads & writes
    Set the expiration year to 2080
    Once the PAR is created, you will be given a URL.
      This will be the only way to get the URL to your Pre-Authenticated Request. Make sure you copy it before you close this window or you will need to re-create the request.

  Terraform Configuration Setup
    Head to Devops/infrastructure/{region} directory, and copy an existing spoke directory to your chosen name. E.G. copy dev to production.
      Open the main.tf file in your newly created Terraform directory.
      Replace the http backend url with the url you copied from the Pre-Authenticated request
    Open the terraform.tfvars file and update the following Variable definitions:
      Pod_name: should match the new configuration name.
      Pod_cidr: Needs to be unique CIDR block.
      Pod_dns_label: Needs to be append with the new configuration name.
      Pod_custom_dns_servers: needs to be updated to reflect the new pod_cidr.
      Pod_search_domain_names: needs to be prefixed with the new configuration name.
    Open the vars.tf file and update the following variable definitions:
      Oci_privnets: Each Maps value needs to be updated with the new configurations CIDR.
      Oci_pubnets: Each Maps value needs to be updated with the new configurations CIDR.

  Hub Configuration requirements
    Open the Devops/Infrastructure/{region}shared-resources/main.tf file.
      In the Object Storage Section Create a Backend Reference to the new configuration
      The terraform block label should be the name of the new configuration
      E.G. data "terraform_remote_state" "new_configuration_name"
      Replace the config address url with the url you copied from the Pre-Authenticated request.

    Open the Devops/infrastructure/{region}/shared-resources/routing.tf file
      Create a Route Rule in every route table necessary for the new Terraform Configuration.
      
Caution & Notes
Every Spoke requires a local peering gateway to the shared-resources VCN & the transit-hub VCN. This is, because OCI does not allow traffic to be routed to a "transit-hub" VCN. Only through it.
When creating a new spoke, after terraform is applied for the new spoke, you must add the corresponding route rules in the shared-resources/routing.tf file and apply that configuration as well.
Core_network should rarely ever be modified, however, if you are to remove or change any object from it, the route rules in the local routing.tf as well as the route rules in the shared-resources/routing.tf must first be removed, terraform applied, then the object can be removed.
