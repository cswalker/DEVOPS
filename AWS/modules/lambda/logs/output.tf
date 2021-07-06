###Outputs

output "logs_lambda_name" {
    value = aws_lambda_function.logs_function.function_name
}

output "logs_lambda_arn" {
    value = aws_lambda_function.logs_function.arn
}