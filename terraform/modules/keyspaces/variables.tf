variable "app_name"      { type = string }
variable "env"           { type = string }
variable "region"        { type = string }
variable "keyspace_name" {
  type    = string
  default = "rental_manager"
}
