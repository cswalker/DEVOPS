# NoSQL table for mapping database servers to private load balancers.
resource "oci_nosql_table" "prod_db_replica_loadbalancers_nosql_table" {
    compartment_id = local.oci_vars["compartments"]["prod"]["compartment_ocid"]
    ddl_statement = "CREATE TABLE db_replica_loadbalancers(database_name STRING, port STRING, load_balancer STRING, PRIMARY KEY(SHARD(database_name)))"
    name = "db_replica_loadbalancers"
    is_auto_reclaimable = false

    table_limits {
      max_read_units = "10"
      max_storage_in_gbs = "1"
      max_write_units = "10"
    }

    defined_tags = {
      "Common.EnvironmentType" = "prod"
    }
}

# NoSQL table for mapping clients to public load balancers.
resource "oci_nosql_table" "prod_client_loadbalancers_nosql_table" {
    compartment_id = local.oci_vars["compartments"]["prod"]["compartment_ocid"]
    ddl_statement = "CREATE TABLE client_loadbalancers(client_name STRING, load_balancer STRING, PRIMARY KEY(SHARD(client_name)))"
    name = "client_loadbalancers"
    is_auto_reclaimable = false

    table_limits {
      max_read_units = "10"
      max_storage_in_gbs = "1"
      max_write_units = "10"
    }

    defined_tags = {
      "Common.EnvironmentType" = "prod"
    }
}
