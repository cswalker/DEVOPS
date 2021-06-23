pipeline {
    agent any

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        choice(name: 'env_type', choices: ['qa', 'uat', 'prod'], description: 'The environment the server is created in')
        string(name: 'env_name', defaultValue: '', description: 'The environment name to build the app from')
        string(name: 'database_name', defaultValue: '', description: 'The database name associated to the cluster')
        string(name: 'primary_db_ip', defaultValue: '', description: 'The primary database server IP address in the cluster')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().environmentName(params.env_name)
                    oci_vars = shared.loadOciVars()

                    replica_lb_info = shared.loadReplicaLoadBalancerInfo().getDatabaseInfo(database_name, env_type)
                    load_balancer_name = replica_lb_info.load_balancer

                    currentBuild.displayName = "#${BUILD_NUMBER} ${env_name}"

                    pgbouncer_server_info = shared.loadInstanceInfo().info(env_name, "pgbouncer", env_type)
                }
            }
        }
        stage('Configure PgBouncer For a Cluster') {
            steps {
                script {
                    script {
                        writeFile file: 'inventory.yaml', text: """[${env_type}]
                        ${pgbouncer_server_info.instance.private_ip}"""
                        sh """
                            ansible-playbook -i inventory.yaml \
                                -e arg_env_type=${env_type} \
                                -e arg_env_name=${env_name} \
                                -e arg_database_name=${database_name} \
                                -e arg_primary_db_ip=${primary_db_ip} \
                                -e arg_replica_loadbalancer_ip=${oci_vars.regions.ashburn[env_type].vcn.loadbalancers[load_balancer_name].ip} \
                                -e arg_replica_loadbalancer_listener_port=${replica_lb_info.port} \
                                provisioners/pgbouncer/ansible/configure.yaml
                        """
                    }
                }
            }
        }
    }
}
