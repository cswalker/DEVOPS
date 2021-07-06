###Initialization
provider "aws" {
  version                   = ">~3.0"
  region                    = local.aws_region
  shared_credentials_file   = var.credentials_file
  profile                   = var.aws_profile
}

terraform {
  backend s3 {
    encrypt = true
    bucket = var.bucket_name
    dynamodb_table = var.state_store
    key = var.state_key
    region = local.aws_region
  }
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "3.8"
    }
    template = {
      source = "hashicorp/template"
      version = "2/1"
    }
  }
  required_version = ">=0.14"
}

module "s3-buckets" {
  source = "../../../modules/s3-buckets"
  environment = local.environment
  aws_region = local.aws_region
}

module "logs" {
  source = "../../../modules/lambda/logs"
  aws_account_number = local.aws_account
  aws_region = local.aws_region
  environment = local.environment
  source_hash = local.aws_vars.logs.source_hash
  source_s3_bucket = data.remotestate.buckets.s3_bucket_name
  source_s3_key = local.aws_vars.logs.source_s3_key
  syslog_est = local.aws_vars.logconsumer.syslog_destination
}