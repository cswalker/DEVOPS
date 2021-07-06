### Logs Lambda

# Lambda Function Resource
resource "aws_lambda_function" "logs_function" {
    function_name       = "${var.environment}-${var.aws_region}-logs-lambda"
    handler             = "handler.main"
    publish             = true
    role                = aws_iam_role.logs_role.arn
    runtime             = "python3.8"
    s3_bucket           = var.source_s3_bucket
    s3_key              = var.source_s3_key
    s3_object_version   = data.aws_s3_bucket_object.logs_source.version_id
    source_code_hash    = var.source_hash
    timeout             = 60
    environment {
        variables   = {
            syslog_dest     = var.syslog_dest
            hostname_prefix = "ECS"
        }
    }
    tags = {
        Environment     = var.environment
    }
}

#Cloudwatch Lambda Permissions
resource "aws_lambda_permission" "logs-cloudwatch" {
    statement_id    = "logs-cloudwatch"
    action          = "lambda:InvokeFunction"
    function_name   = aws_lambda_function.logs_function.arn
    principal       = "logs.${var.aws_region}:${var.aws_account_number}:log-group:*:*"
}