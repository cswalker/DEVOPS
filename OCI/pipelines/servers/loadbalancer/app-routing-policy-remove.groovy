import groovy.json.JsonSlurper
pipeline {
    agent any

    environment {
        ENV_TYPE = 'prod'
        LOADBALANCER_TYPE = 'app'
    }

    parameters {
        choice(name: 'job', choices: ['run', 'init'], description: 'Should the job be initialized or ran')
        string(name: 'client_name', defaultValue: '', description: 'Provide the client name this load balancer is associated to')
        string(name: 'routing_policy_name', defaultValue: '', description: 'The routing policy name to remove')
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

                    currentBuild.displayName = "#${BUILD_NUMBER} ${client_name}_${LOADBALANCER_TYPE}"
            
                    def compartment_loadbalancers = shared.loadOciDynamicInfo().getCompartmentLoadBalancers(ENV_TYPE)

                    def loadbalancers = compartment_loadbalancers.findAll {
                        it['display-name'] == "${params.client_name}_${LOADBALANCER_TYPE}"
                    }

                    app_loadbalancer = loadbalancers[0]
                }
            }
        }

        stage('Remove App Load Balancer Routing Policy') {
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
                                    loadbalancer_ocid: app_loadbalancer.id,
                                    routing_policy_name: params.routing_policy_name,
                                ]
                            ]
                        ]
                    ]
                    
                    writeYaml(file: "inventory.yaml", data: inventory)

                    sh """
                        ansible-playbook -i inventory.yaml \
                            provisioners/loadbalancer/ansible/app-routing-policy-remove.yaml
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
