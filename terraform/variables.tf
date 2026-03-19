variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "env" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "app_name" {
  description = "Application name used for resource naming"
  type        = string
  default     = "munas-property-manager"
}

variable "image_tag" {
  description = "Docker image tag to deploy (Git SHA)"
  type        = string
  default     = "latest"
}
