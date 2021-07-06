### Output

output "s3_bucket_name" {
    value = aws_s3_bucket.lambda_content.name
}

output "s3_bucket_arn" {
    value = aws_s3_bucket.lambda_content.arn
}