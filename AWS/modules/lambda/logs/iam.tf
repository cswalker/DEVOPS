### Logs IAM Role

#Logs IAM Role
resource "aws_iam_role" "logs_role" {
    name                = "${var.environment}-${var.aws_region}-logs-role"
    assume_role_policy  = <<EOF
    {
        "version": "2012-10-17",
        "Statement": [
            {
                "Action": "sts:AssumeRole",
                "Principal": {
                    "Service": "lambda.amazonaws.com"
                },
                "Effect": "Allow",
                "Sid": ""
            }
        ]
    }
    EOF
}

#Logs IAM Policy
resource "aws_iam_policy" "logs_policy" {
    name            = "${var.environment}-${var.aws_region}-logs-policy"
    path            = "/"
    description     = "IAM Policy for Logs Lambda"
    policy          = <<EOF
    {
        "version": "2012-10-17",
        "Statement": [
            {
                "Action": [
                    "logs:CreateLogGroup",
                    "logs:CreateLogStream",
                    "logs:PutLogEvents"
                ],
                "Resource": "arn:aws:logs:*:*:*",
                "Effect": "Allow"
            }
        ]
    }
    EOF
}

#Logs Policy Attachment to Role
resource "aws_iam_role_policy_attachment" "logs_attachment" {
    role        = aws_iam_role.logs_role.name
    policy_arn  = aws_iam_policy.logs_policy.arn
}