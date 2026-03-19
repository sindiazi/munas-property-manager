output "alb_dns"          { value = aws_lb.app.dns_name }
output "target_group_arn" { value = aws_lb_target_group.app.arn }
output "alb_arn_suffix"   { value = aws_lb.app.arn_suffix }
output "alerts_topic_arn" { value = aws_sns_topic.alerts.arn }
