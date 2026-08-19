# GameCenter Backend 🚀

A high-performance, serverless Spring Boot backend for managing games, projects, and dynamic questionnaires on AWS. 

## Overview
This repository contains the core API services for the GameCenter. Built on top of **Spring Boot 3** and optimized for serverless deployments using **AWS Lambda** and **AWS SAM**, it provides scalable endpoints for managing users, projects, and batch importing questions.

## Features
- **Serverless Architecture**: Deployed as AWS Lambda functions triggered by API Gateway and S3 events.
- **Optimized Performance**: Configured for fast cold starts using AWS SnapStart and Tiered Compilation (`-XX:TieredStopAtLevel=1`).
- **NoSQL Data Layer**: Utilizes AWS DynamoDB for highly scalable data storage (Projects, Users, Questions).
- **Batch Processing**: Event-driven S3 batch import capabilities for questionnaires and game data.
- **Secure Authentication**: Built-in JWT-based authentication and role-based access control (Super Admin vs. Project Admin).
- **Interactive Documentation**: Swagger UI automatically integrated via SpringDoc OpenAPI.

## Tech Stack
- **Java 17**
- **Spring Boot 3.2.3**
- **AWS Serverless Java Container**
- **AWS DynamoDB & S3**
- **AWS SAM (Serverless Application Model)**
- **JSON Web Tokens (JWT)**

## Prerequisites
- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [AWS CLI](https://aws.amazon.com/cli/) configured with appropriate IAM permissions
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)

## Getting Started

### Local Development
1. **Build the project:**
   ```bash
   ./mvnw clean package
   ```
2. **Run locally with SAM CLI:**
   ```bash
   sam local start-api
   ```
   *Note: Configure necessary environment variables (like `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, and `AWS_S3_IMPORT_BUCKET_NAME`) in a `env.json` file for local testing.*

### Deployment
To package and deploy the backend to AWS using SAM:

You can use the included deployment script:
```bash
./deploy.sh
```
Or manually run:
```bash
sam build
sam deploy --guided
```

## Environment Variables
Key environment variables required for deployment:
- `JWT_SECRET`: Secret key used for signing JWT tokens.
- `CORS_ALLOWED_ORIGINS`: Allowed origins for CORS (e.g., `http://localhost:3000`).
- `AWS_S3_IMPORT_BUCKET_NAME`: The globally unique name of your S3 bucket for question imports.

## API Documentation
Once the application is running, access the interactive API documentation at:
- Swagger UI: `GET /swagger-ui.html`
- OpenAPI JSON: `GET /v3/api-docs`
