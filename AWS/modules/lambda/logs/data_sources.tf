### Data Sources

#Retrieve S3 Bucket Details for Lambda
data "aws_s3_bucket_object" "logconsumer_source" {
    bucket  = var.source_s3_bucket
    key     = var.source_s3_key
}