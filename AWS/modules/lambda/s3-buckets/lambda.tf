###S3 Lambda

# Lambda Bucket Content
resource "aws_s3_bucket" "lambda_content" {
    acl             = "private"
    bucket          = "${var.environment}-${var.aws_region}-lambdas"
    force_destroy   = false
    tags = {
        Name        = "${var.environtment}-${var.aws_region}-lambdas"
        Environment = "${var.environment}"
    }
    server_side_encryption_configuration {
        rule {
            apply_server_side_encryption_by_default {
                sse_algorithm = "AE256"
            }
        }
    }
    versioning {
        enabled = true
    }
}

# Bucket Policy
resource "aws_s3_bucket_policy" "lambda_bucket_policy" {
    bucket = aws_s3_bucket.lambda_content.id
    policy = <<END_POLICY
    {
        "Version": 2012-10-17",
        "Statement": [
            {
                "Effect": "Deny",
                "Principal": "*",
                "Action: "s3:*",
                "Resource": [
                    "arn:aws:s3:::${var.environment}-${var.aws_region}-lambdas",
                    "arn:aws:s3:::${var.environment}-${var.aws_region}-lambdas/*"
                ],
                "Condition": {
                    "Bool": {
                        "aws:SecureTransport": "false"
                    }
                }
            }
        ]
    }
    END_POLICY
}