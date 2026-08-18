#!/bin/bash
set -e

# Go to the backend directory
cd "$(dirname "$0")"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

echo "Building Java application..."
./mvnw clean package -DskipTests

JAR="target/serverless-0.0.1-SNAPSHOT-aws.jar"
BUCKET_NAME="gamecenter-cf-deployments-774411"
REGION="ap-south-1"
STACK_NAME="gamecenter-serverless"

# Compute S3 key from JAR hash so CloudFormation always picks up a fresh upload
S3_KEY=$(md5sum "$JAR" | cut -d' ' -f1)
echo "Uploading JAR ($(du -sh "$JAR" | cut -f1)) to s3://$BUCKET_NAME/$S3_KEY ..."
aws s3 cp "$JAR" "s3://$BUCKET_NAME/$S3_KEY" --region "$REGION"

# Replace CodeUri in template so cloudformation package resolves the right file
sed "s|CodeUri: target/serverless-0.0.1-SNAPSHOT-aws.jar|CodeUri: s3://$BUCKET_NAME/$S3_KEY|g" template.yaml > /tmp/template_patched.yaml

echo "Deploying infrastructure via CloudFormation..."
aws cloudformation deploy \
  --template-file /tmp/template_patched.yaml \
  --stack-name "$STACK_NAME" \
  --capabilities CAPABILITY_IAM \
  --parameter-overrides JwtSecret="$JWT_SECRET" \
  CorsAllowOrigins="$CORS_ALLOWED_ORIGINS" \
  ImportBucketName="$AWS_S3_IMPORT_BUCKET_NAME"


echo "✅ Deployment Successful!"