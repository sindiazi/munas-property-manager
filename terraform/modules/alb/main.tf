locals {
  name_prefix = "${var.app_name}-${var.env}"
}

# ── ALB ───────────────────────────────────────────────────────────────────────

resource "aws_lb" "app" {
  name               = "${local.name_prefix}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [var.alb_sg_id]
  subnets            = var.subnet_ids

  tags = { Name = "${local.name_prefix}-alb" }
}

resource "aws_lb_target_group" "app" {
  name        = "${local.name_prefix}-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    port                = "8080"
    protocol            = "HTTP"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 30
    timeout             = 10
    matcher             = "200"
  }

  tags = { Name = "${local.name_prefix}-tg" }
}

# HTTP listener — forwards directly (no TLS; use ALB DNS name for testing)
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# ── CloudWatch Alarms ─────────────────────────────────────────────────────────

resource "aws_sns_topic" "alerts" {
  name = "${local.name_prefix}-alerts"
}

resource "aws_cloudwatch_metric_alarm" "alb_5xx" {
  alarm_name          = "${local.name_prefix}-alb-5xx-rate"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  threshold           = 1
  alarm_description   = "ALB 5xx error rate > 1%"
  alarm_actions       = [aws_sns_topic.alerts.arn]
  treat_missing_data  = "notBreaching"

  metric_query {
    id          = "error_rate"
    expression  = "100*(m2/m1)"
    label       = "5xx Error Rate (%)"
    return_data = true
  }

  metric_query {
    id = "m1"
    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "RequestCount"
      dimensions  = { LoadBalancer = aws_lb.app.arn_suffix }
      period      = 60
      stat        = "Sum"
    }
  }

  metric_query {
    id = "m2"
    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "HTTPCode_Target_5XX_Count"
      dimensions  = { LoadBalancer = aws_lb.app.arn_suffix }
      period      = 60
      stat        = "Sum"
    }
  }
}
