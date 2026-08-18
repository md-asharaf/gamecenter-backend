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
BUCKET_NAME="vocabkicker-cf-deployments-774411"
REGION="ap-south-1"
STACK_NAME="vocabkicker-serverless"

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
  CorsAllowOrigin="$CORS_ALLOW_ORIGIN" \
  ImportBucketName="$IMPORT_BUCKET_NAME"


# Force-update all Lambda function code to the correct JAR
echo "Updating Lambda function code..."
FUNCTIONS=(
  "AdminLoginFunction"
  "CreateAdminFunction"
  "AdminRefreshFunction"
  "BatchCreateQuestionsFunction"
  "GenerateUploadUrlFunction"
  "GetQuestionsFunction"
  "GenerateQuizFunction"
  "GetQuestionByIdFunction"
  "UpdateQuestionFunction"
  "DeleteQuestionFunction"
  "ListAdminsFunction"
  "UpdateAdminFunction"
  "DeleteAdminFunction"
)

for LOGICAL_NAME in "${FUNCTIONS[@]}"; do
  PHYSICAL=$(aws cloudformation describe-stack-resources \
    --stack-name "$STACK_NAME" \
    --logical-resource-id "$LOGICAL_NAME" \
    --region "$REGION" \
    --query 'StackResources[0].PhysicalResourceId' \
    --output text)
  echo "  Updating $LOGICAL_NAME ($PHYSICAL)..."
  aws lambda update-function-code \
    --function-name "$PHYSICAL" \
    --s3-bucket "$BUCKET_NAME" \
    --s3-key "$S3_KEY" \
    --region "$REGION" \
    --query 'LastUpdateStatus' \
    --output text
done

echo "✅ Deployment Successful!"