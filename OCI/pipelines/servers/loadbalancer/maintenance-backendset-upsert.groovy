pipeline {
    agent any

    environment {
        ENV_TYPE          = 'prod'
        LOADBALANCER_TYPE = 'app'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        string(name: 'client_name', defaultValue: '', description: 'The client name the load balancer is associated with')
    }

    stages {
        stage('Init') {
            steps {
                script {
                    shared = load pwd() + "/pipelines/shared/autoload.groovy"
                    shared.loadValidators().init(params.job)
                    shared.loadValidators().clientName(params.client_name)

                    // The writeYaml function won't overwrite existing files, so we need
                    // to clean up the inventory file in case a previous run did not clean it up.
                    sh "rm -f inventory.yaml"

                    currentBuild.displayName = "#${BUILD_NUMBER} ${params.client_name} ${params.release_version}"

                    def compartment_loadbalancers = shared.loadOciDynamicInfo().getCompartmentLoadBalancers(ENV_TYPE)

                    def loadbalancers = compartment_loadbalancers.findAll {
                        it['display-name'] == "${params.client_name}_${LOADBALANCER_TYPE}"
                    }

                    app_loadbalancer = loadbalancers[0]
                
                    def compartment_instances = shared.loadOciDynamicInfo().getCompartmentInstances(ENV_TYPE)

                    def maintenance_instances = compartment_instances.findAll {
                        it['defined-tags']['Common'] != null &&
                        it['defined-tags']['Common']['EnvironmentName'] == "shared" &&
                        it['defined-tags']['Common']['EnvironmentType'] == ENV_TYPE &&
                        it['defined-tags']['Instances'] != null &&
                        it['defined-tags']['Instances']['ServerType'] == "maintenance"
                    }

                    maintenance_servers = shared.loadOciDynamicInfo().getInstanceIps(maintenance_instances)

                    backends = []
                    for (ip in maintenance_servers) {
                        backends << [
                            ip_address: ip,
                            port: "443" 
                        ]
                    }
                }
            }
        }

        stage('Create Maintenance Backend Set') {
            steps {
                script {
                    wrap([$class: 'BuildUser']) {
                        build_user = "${BUILD_USER_ID}"
                    }

                    def inventory = [
                        "${ENV_TYPE}": [
                            hosts: [
                                localhost: [
                                    ansible_python_interpreter: "/usr/bin/python3",
                                    client_name: params.client_name,
                                    env_type: ENV_TYPE,
                                    loadbalancer_ocid: app_loadbalancer.id,
                                    loadbalancer_certificate_name: app_loadbalancer.certificates.keySet()[0],
                                    app_loadbalancer_backends: backends,
                                    app_loadbalancer_backendset_name: "maintenance",
                                ]
                            ]
                        ]
                    ]
                    
                    writeYaml(file: "inventory.yaml", data: inventory)

                    sh """
                        ansible-playbook -i inventory.yaml \
                            provisioners/loadbalancer/ansible/app-backendset-upsert.yaml
                    """
                }
            }
        }
    }

    post {
        cleanup {
            // The writeYaml function won't overwrite existing files, so we need
            // to clean up the inventory file so successive runs won't fail.
            sh "rm -f inventory.yaml"
        }
    }
}
