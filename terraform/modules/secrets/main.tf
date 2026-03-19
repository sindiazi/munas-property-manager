locals {
  name_prefix = "${var.app_name}/${var.env}"

  secrets = {
    jwt_secret          = "JWT_SECRET"
    ssn_encryption_key  = "SSN_ENCRYPTION_KEY"
    mpesa_consumer_key  = "MPESA_CONSUMER_KEY"
    mpesa_consumer_secret = "MPESA_CONSUMER_SECRET"
    mpesa_short_code    = "MPESA_SHORT_CODE"
    mpesa_passkey       = "MPESA_PASSKEY"
    keyspaces_username  = "KEYSPACES_USERNAME"
    keyspaces_password  = "KEYSPACES_PASSWORD"
  }
}

resource "aws_secretsmanager_secret" "app" {
  for_each = local.secrets

  name        = "${local.name_prefix}/${each.value}"
  description = "${each.value} for ${var.app_name} ${var.env}"

  tags = { Name = "${local.name_prefix}/${each.value}" }
}
