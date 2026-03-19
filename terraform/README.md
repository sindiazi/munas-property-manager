# Terraform — Infrastructure as Code

## Prerequisites

- AWS CLI configured with an IAM user/role that has admin permissions
- Terraform >= 1.7
- An existing Route 53 hosted zone for your domain

## One-time bootstrap: Remote State

Before running `terraform init` for the first time, create the S3 bucket and DynamoDB table for remote state:

```bash
export AWS_REGION="us-east-1"

# S3 bucket (versioning + encryption enforced)
aws s3api create-bucket \
  --bucket munas-property-manager-tfstate \
  --region $AWS_REGION
# Note: --create-bucket-configuration is NOT used for us-east-1 (it's the S3 default region)

aws s3api put-bucket-versioning \
  --bucket munas-property-manager-tfstate \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket munas-property-manager-tfstate \
  --server-side-encryption-configuration \
    '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws s3api put-public-access-block \
  --bucket munas-property-manager-tfstate \
  --public-access-block-configuration \
    BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

# DynamoDB table for state locking
aws dynamodb create-table \
  --table-name munas-property-manager-tf-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region $AWS_REGION
```

## One-time bootstrap: GitHub Actions OIDC IAM Role

Create an IAM role that GitHub Actions can assume via OIDC (no long-lived keys):

```bash
# 1. Create the OIDC provider (one per AWS account, not per repo)
aws iam create-open-id-connect-provider \
  --url https://token.actions.githubusercontent.com \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1

# 2. Create trust policy — replace YOUR_GITHUB_ORG/REPO
cat > /tmp/trust-policy.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:YOUR_GITHUB_ORG/munas-property-manager:*"
        }
      }
    }
  ]
}
EOF

aws iam create-role \
  --role-name GitHubActionsDeployRole \
  --assume-role-policy-document file:///tmp/trust-policy.json

# 3. Attach permissions (AdministratorAccess for IaC; scope down for production)
aws iam attach-role-policy \
  --role-name GitHubActionsDeployRole \
  --policy-arn arn:aws:iam::aws:policy/AdministratorAccess
```

> **Scope down for production**: replace AdministratorAccess with a custom policy that covers
> ECR push, ECS update-service, Secrets Manager read, and Terraform state S3/DynamoDB access.

## GitHub Secrets Required

Set the following in your GitHub repo → Settings → Secrets → Actions:

| Secret | Value |
|---|---|
| `AWS_ACCOUNT_ID` | Your 12-digit AWS account ID |
| `AWS_REGION` | e.g. `eu-west-1` |
| `OIDC_ROLE_ARN` | ARN of `GitHubActionsDeployRole` |

## Populate Secrets Manager

After `terraform apply`, populate each secret value via AWS Console or CLI:

```bash
ENV="prod"
APP="munas-property-manager"

aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/JWT_SECRET" \
  --secret-string "$(openssl rand -base64 64)"

aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/SSN_ENCRYPTION_KEY" \
  --secret-string "$(openssl rand -base64 32)"

# MPESA credentials: copy from Safaricom Daraja portal
aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/MPESA_CONSUMER_KEY" --secret-string "YOUR_VALUE"

aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/MPESA_CONSUMER_SECRET" --secret-string "YOUR_VALUE"

aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/MPESA_SHORT_CODE" --secret-string "YOUR_VALUE"

aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/MPESA_PASSKEY" --secret-string "YOUR_VALUE"

# Keyspaces credentials: generate from AWS Console → Amazon Keyspaces → Generate credentials
aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/KEYSPACES_USERNAME" --secret-string "YOUR_VALUE"

aws secretsmanager put-secret-value \
  --secret-id "$APP/$ENV/KEYSPACES_PASSWORD" --secret-string "YOUR_VALUE"
```

## Normal usage

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

## Cost optimisation (Fargate Spot)

To cut compute cost from ~$35/month to ~$11/month, edit `modules/ecs/main.tf`:
Change `launch_type = "FARGATE"` to a capacity provider strategy using `FARGATE_SPOT`.
The app is stateless, so Spot interruptions just trigger a re-deploy from ECR.
