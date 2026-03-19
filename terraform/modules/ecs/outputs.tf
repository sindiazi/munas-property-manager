output "cluster_name"    { value = aws_ecs_cluster.app.name }
output "cluster_arn"     { value = aws_ecs_cluster.app.arn }
output "service_name"    { value = aws_ecs_service.app.name }
output "task_exec_role"  { value = aws_iam_role.task_execution.arn }
output "task_role"       { value = aws_iam_role.task.arn }
