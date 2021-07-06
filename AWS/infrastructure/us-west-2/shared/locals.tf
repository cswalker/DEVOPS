### Local Variable Definitions
locals {
aws_vars = yamldecode(file("../../../configurations/vars.yaml"))
aws_account = local.aws_vars["global"]["account"]["subprod"]
aws_region = local.aws_vars["global"]["region"]["secondary"]
environment = local.aws_vars["environment"]["shared"]
}
