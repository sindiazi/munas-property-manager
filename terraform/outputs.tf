output "alb_dns" {
  description = "ALB DNS name"
  value       = module.alb.alb_dns
}

output "ecr_url" {
  description = "ECR repository URL"
  value       = module.ecr.repository_url
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs.cluster_name
}
