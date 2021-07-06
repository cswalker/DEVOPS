### Datasources

# Import from module
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
