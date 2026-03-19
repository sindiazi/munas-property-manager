module "networking" {
  source   = "./modules/networking"
  app_name = var.app_name
  env      = var.env
  region   = var.region
}

module "ecr" {
  source   = "./modules/ecr"
  app_name = var.app_name
  env      = var.env
}

module "alb" {
  source      = "./modules/alb"
  app_name    = var.app_name
  env         = var.env
  vpc_id      = module.networking.vpc_id
  subnet_ids  = module.networking.public_subnet_ids
  alb_sg_id   = module.networking.alb_sg_id
  region      = var.region
}

module "secrets" {
  source   = "./modules/secrets"
  app_name = var.app_name
  env      = var.env
  region   = var.region
}

module "keyspaces" {
  source      = "./modules/keyspaces"
  app_name    = var.app_name
  env         = var.env
  region      = var.region
}

module "ecs" {
  source              = "./modules/ecs"
  app_name            = var.app_name
  env                 = var.env
  region              = var.region
  vpc_id              = module.networking.vpc_id
  subnet_ids          = module.networking.public_subnet_ids
  ecs_sg_id           = module.networking.ecs_sg_id
  ecr_url             = module.ecr.repository_url
  image_tag           = var.image_tag
  alb_target_group_arn = module.alb.target_group_arn
  secrets_arns        = module.secrets.secret_arns
  keyspace_arn        = module.keyspaces.keyspace_arn
}
