locals {
  name_prefix = "${var.app_name}-${var.env}"
}

# ── CloudWatch Log Group ──────────────────────────────────────────────────────

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name_prefix}"
  retention_in_days = 30

  tags = { Name = "${local.name_prefix}-logs" }
}

# ── IAM: Task Execution Role (ECS agent pulls image + reads secrets) ──────────

data "aws_iam_policy_document" "ecs_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "task_execution" {
  name               = "${local.name_prefix}-task-exec-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
}

resource "aws_iam_role_policy_attachment" "task_execution_managed" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "task_execution_extras" {
  statement {
    sid     = "SecretsRead"
    actions = ["secretsmanager:GetSecretValue"]
    resources = values(var.secrets_arns)
  }
}

resource "aws_iam_role_policy" "task_execution_extras" {
  name   = "${local.name_prefix}-exec-extras"
  role   = aws_iam_role.task_execution.id
  policy = data.aws_iam_policy_document.task_execution_extras.json
}

# ── IAM: Task Role (app runtime — Keyspaces + Secrets Manager) ────────────────

resource "aws_iam_role" "task" {
  name               = "${local.name_prefix}-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
}

data "aws_iam_policy_document" "task_policy" {
  statement {
    sid       = "KeyspacesAccess"
    actions   = ["cassandra:*"]
    resources = [var.keyspace_arn, "${var.keyspace_arn}/*"]
  }

  statement {
    sid     = "SecretsRead"
    actions = ["secretsmanager:GetSecretValue"]
    resources = values(var.secrets_arns)
  }
}

resource "aws_iam_role_policy" "task" {
  name   = "${local.name_prefix}-task-policy"
  role   = aws_iam_role.task.id
  policy = data.aws_iam_policy_document.task_policy.json
}

# ── ECS Cluster ───────────────────────────────────────────────────────────────

resource "aws_ecs_cluster" "app" {
  name = "${local.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = { Name = "${local.name_prefix}-cluster" }
}

resource "aws_ecs_cluster_capacity_providers" "app" {
  cluster_name = aws_ecs_cluster.app.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }
}

# ── ECS Task Definition ───────────────────────────────────────────────────────

resource "aws_ecs_task_definition" "app" {
  family                   = "${local.name_prefix}-task"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "1024"
  memory                   = "2048"
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([
    {
      name      = var.app_name
      image     = "${var.ecr_url}:${var.image_tag}"
      essential = true

      portMappings = [
        { containerPort = 8080, protocol = "tcp" }
      ]

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "CASSANDRA_HOST", value = "cassandra.${var.region}.amazonaws.com" },
        { name = "CASSANDRA_PORT", value = "9142" },
        { name = "CASSANDRA_DC", value = var.region }
      ]

      secrets = [
        { name = "JWT_SECRET",            valueFrom = var.secrets_arns["jwt_secret"] },
        { name = "SSN_ENCRYPTION_KEY",    valueFrom = var.secrets_arns["ssn_encryption_key"] },
        { name = "MPESA_CONSUMER_KEY",    valueFrom = var.secrets_arns["mpesa_consumer_key"] },
        { name = "MPESA_CONSUMER_SECRET", valueFrom = var.secrets_arns["mpesa_consumer_secret"] },
        { name = "MPESA_SHORT_CODE",      valueFrom = var.secrets_arns["mpesa_short_code"] },
        { name = "MPESA_PASSKEY",         valueFrom = var.secrets_arns["mpesa_passkey"] },
        { name = "KEYSPACES_USERNAME",    valueFrom = var.secrets_arns["keyspaces_username"] },
        { name = "KEYSPACES_PASSWORD",    valueFrom = var.secrets_arns["keyspaces_password"] }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1"]
        interval    = 30
        timeout     = 10
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = { Name = "${local.name_prefix}-task" }
}

# ── ECS Service ───────────────────────────────────────────────────────────────

resource "aws_ecs_service" "app" {
  name                               = "${local.name_prefix}-service"
  cluster                            = aws_ecs_cluster.app.id
  task_definition                    = aws_ecs_task_definition.app.arn
  desired_count                      = 1
  launch_type                        = "FARGATE"
  health_check_grace_period_seconds  = 120

  network_configuration {
    subnets          = var.subnet_ids
    security_groups  = [var.ecs_sg_id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = var.alb_target_group_arn
    container_name   = var.app_name
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  tags = { Name = "${local.name_prefix}-service" }

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}

# ── CloudWatch Alarm: ECS task count < 1 ─────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "ecs_task_count" {
  alarm_name          = "${local.name_prefix}-ecs-no-tasks"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "RunningTaskCount"
  namespace           = "ECS/ContainerInsights"
  period              = 60
  statistic           = "Average"
  threshold           = 1
  alarm_description   = "ECS running task count dropped below 1"
  treat_missing_data  = "breaching"

  dimensions = {
    ClusterName = aws_ecs_cluster.app.name
    ServiceName = aws_ecs_service.app.name
  }
}
