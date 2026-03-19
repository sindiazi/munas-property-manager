resource "aws_keyspaces_keyspace" "app" {
  name = var.keyspace_name

  tags = {
    Name = var.keyspace_name
    env  = var.env
  }
}
