Ansible Documentation

     https://docs.ansible.com/

Oracle Ansible Documentation

     https://docs.ansible.com/ansible/latest/scenario_guides/guide_oracle.html

Ansible Configurations: 

  /provisioners


Ansible Devops

The Ansible Configuration Facilitates the Creation of Instance Creation, Configuration, & Destruction within Oracle Cloud Infrastructure. The authentication type requires the code be initialized and applied from an instance principal within OCI itself. This instance principal here is currently Jenkins.

Directory Structure

Within the Devops Git Repository, all of the Ansible Code is found within the provisioners directory subdivided by instance role/type. Each Role has a variables directory, a roles directory, and the playbooks. 
