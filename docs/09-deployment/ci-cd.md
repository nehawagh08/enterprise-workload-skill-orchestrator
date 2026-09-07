# CI/CD Pipeline & Deployment Strategy

## 1. Purpose

This document defines the Continuous Integration and Continuous Deployment (CI/CD) strategy for the **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)**.

The CI/CD pipeline automates:

* Source code validation
* Compilation
* Unit testing
* Integration testing
* Code coverage
* Static analysis
* Dependency security scanning
* Docker image creation
* Container image scanning
* Image publishing
* Environment deployment
* Health verification
* Rollback

The goal is to create a deployment process similar to the practices used in enterprise software development teams.

---

# 2. CI/CD Architecture

The overall pipeline is:

```text
Developer
    |
    v
Git Branch
    |
    v
Pull Request
    |
    v
GitHub
    |
    v
GitHub Actions
    |
    +----------------------+
    |                      |
    v                      v
Build                  Code Quality
    |                      |
    v                      v
Unit Tests            Static Analysis
    |                      |
    +----------+-----------+
               |
               v
       Integration Tests
               |
               v
       Security Scanning
               |
               v
        Docker Build
               |
               v
      Container Scan
               |
               v
        Amazon ECR
               |
               v
       ECS Deployment
               |
               v
       Health Checks
               |
               v
          Production
```

---

# 3. CI vs CD

## Continuous Integration

CI validates every code change.

```text
Code
 ↓
Build
 ↓
Test
 ↓
Analyze
 ↓
Security Scan
```

The objective is to prevent broken code from being merged.

## Continuous Deployment

CD takes validated code and deploys it.

```text
Validated Code
      ↓
Docker Image
      ↓
ECR
      ↓
ECS
      ↓
Health Check
      ↓
Production
```

---

# 4. GitHub Actions

GitHub Actions is the primary CI/CD automation platform.

Recommended workflow files:

```text id="f1t7ya"
.github/
└── workflows/
    ├── ci.yml
    ├── docker.yml
    ├── security.yml
    └── deploy.yml
```

For a smaller implementation, these can initially be combined into one pipeline.

---

# 5. Branching Strategy

Recommended Git workflow:

```text
main
 |
 +--- develop
       |
       +--- feature/task-service
       |
       +--- feature/recommendation-engine
       |
       +--- feature/workload-service
```

### Main

Production-ready code.

### Develop

Integration branch for completed features.

### Feature branches

Used for individual development tasks.

Example:

```text
feature/recommendation-scoring
feature/task-management
feature/employee-skills
feature/workload-calculation
```

---

# 6. Pull Request Workflow

A feature should follow:

```text
Create Feature Branch
        |
        v
Implement Feature
        |
        v
Run Local Tests
        |
        v
Push Branch
        |
        v
Create Pull Request
        |
        v
GitHub Actions CI
        |
        v
Code Review
        |
        v
Merge
```

A pull request should not be merged if mandatory CI checks fail.

---

# 7. Continuous Integration Pipeline

The CI pipeline should execute:

```text
Checkout
   ↓
Set up Java
   ↓
Dependency Cache
   ↓
Compile
   ↓
Unit Tests
   ↓
Integration Tests
   ↓
Coverage
   ↓
Static Analysis
   ↓
Dependency Scan
```

---

# 8. Java Build

EWSO uses Java 21.

The pipeline should explicitly configure Java 21.

Example:

```yaml id="8qk0p7"
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: '21'
    cache: maven
```

This ensures that CI uses the same Java generation as the application.

---

# 9. Maven Build

The pipeline should execute:

```bash id="x6zv0v"
mvn clean verify
```

This provides:

* Compilation
* Unit tests
* Integration tests configured for the project
* Packaging
* Verification

Individual services can also be built independently.

---

# 10. Unit Testing

Unit tests should run on every pull request.

Example:

```bash id="4d7z3a"
mvn test
```

The tests cover:

* Business rules
* Services
* Recommendation scoring
* Eligibility
* Ranking
* Workload calculations
* Validation
* Exception handling

---

# 11. Recommendation Engine CI Validation

The recommendation engine is business-critical.

The pipeline should validate:

```text id="1c4i8p"
Mandatory Skills
       ↓
Eligibility
       ↓
Skill Score
       ↓
Experience
       ↓
Workload
       ↓
Availability
       ↓
Performance
       ↓
Learning Opportunity
       ↓
Final Score
```

Regression tests should ensure that changes to the scoring algorithm do not unexpectedly change business behavior.

---

# 12. Integration Testing

Integration tests verify communication between application components.

Examples:

```text id="8x0g8g"
Task Service
    ↓
Database

Recommendation Service
    ↓
Employee Service

Recommendation Service
    ↓
Redis

Task Service
    ↓
Kafka
```

Testcontainers can provide:

* MySQL
* Redis
* Kafka

during CI.

---

# 13. Testcontainers

The CI environment should be capable of running:

```text id="6exv44"
MySQL Container
Redis Container
Kafka Container
```

for integration tests.

This reduces the difference between:

```text
Developer Environment
        |
        v
CI Environment
```

---

# 14. Code Coverage

JaCoCo should generate code coverage reports.

Example:

```text id="2s3ny1"
Unit Tests
     |
     v
JaCoCo
     |
     v
Coverage Report
```

The project target is:

```text id="5w9d3m"
Core business logic ≥ 80%
```

Coverage should focus on meaningful business logic rather than attempting to maximize a raw percentage.

---

# 15. Static Code Analysis

SonarQube or an equivalent static analysis tool can inspect:

* Code smells
* Bugs
* Vulnerabilities
* Duplicated code
* Maintainability
* Complexity
* Test coverage

Pipeline:

```text id="r3abk4"
Build
 ↓
Test
 ↓
JaCoCo
 ↓
Sonar Analysis
```

A quality gate can prevent problematic code from being merged.

---

# 16. Dependency Security

Dependencies should be scanned for known vulnerabilities.

The pipeline should inspect:

```text id="y2b9dr"
Spring Boot
Spring Cloud
Jackson
Netty
Kafka libraries
Redis libraries
MySQL drivers
Frontend dependencies
```

Possible tools include:

* OWASP Dependency-Check
* GitHub Dependabot
* Snyk
* Trivy

The exact tool can be selected during implementation.

---

# 17. Secret Scanning

The repository should be checked for accidentally committed secrets.

Potential secrets include:

```text id="5x8z0a"
AWS credentials
JWT secrets
Database passwords
PayPal credentials
LLM API keys
Kafka credentials
```

Secrets must never be stored in source code.

---

# 18. Docker Build Pipeline

After CI succeeds:

```text
Source Code
    |
    v
Docker Build
    |
    v
Docker Image
```

Each deployable microservice can produce its own image.

Example:

```text id="0o0v6f"
ewso-auth-service
ewso-employee-service
ewso-task-service
ewso-recommendation-service
```

---

# 19. Docker Image Tagging

Images should use immutable version identifiers.

Recommended tags:

```text id="j6n6ad"
1.0.0
1.0.1
1.1.0
```

or commit-based tags:

```text id="c9l4n8"
sha-a81f27c
sha-b729d10
```

A useful production strategy is to deploy using the Git commit SHA.

Example:

```text id="0r3b9p"
ewso/recommendation-service:a81f27c
```

This makes it possible to identify exactly which source version is running.

---

# 20. Amazon ECR

After the Docker build:

```text id="5p3p4j"
Docker Image
     |
     v
Amazon ECR
```

Each service can have an ECR repository.

Example:

```text id="jkn1z0"
ewso-api-gateway
ewso-auth-service
ewso-employee-service
ewso-skill-service
ewso-project-service
ewso-task-service
ewso-workload-service
ewso-recommendation-service
ewso-performance-service
ewso-notification-service
ewso-analytics-service
```

---

# 21. AWS Authentication

GitHub Actions should avoid storing long-lived AWS access keys whenever possible.

The recommended approach is:

```text id="r5f31w"
GitHub Actions
       |
       v
OIDC
       |
       v
AWS IAM Role
       |
       v
AWS Resources
```

This allows GitHub Actions to obtain temporary AWS credentials.

---

# 22. IAM Deployment Role

GitHub Actions should use a dedicated deployment role.

The role should have only the permissions required to:

* Push images to ECR
* Update ECS services
* Register task definitions
* Read required deployment information

It should not have unrestricted administrator permissions.

---

# 23. ECS Deployment

After an image is pushed:

```text id="t8i5y1"
ECR
 |
 v
ECS Task Definition
 |
 v
ECS Service
 |
 v
New Container
```

The ECS task definition specifies:

* Container image
* CPU
* Memory
* Ports
* Environment variables
* Secrets
* Logging
* IAM task role
* Health checks

---

# 24. Deployment Strategy

The initial deployment strategy should use rolling deployments.

Example:

```text id="8jjp93"
Current Version
   |
   +--> Task 1
   +--> Task 2

        ↓

New Version
   |
   +--> Task 3
   +--> Task 4

        ↓

Health Checks

        ↓

Old Tasks Removed
```

This reduces downtime.

---

# 25. Deployment Order

Not every service needs to deploy simultaneously.

Recommended approach:

```text id="qj7n7u"
Infrastructure
     ↓
Service Registry
     ↓
Auth
     ↓
Core Services
     ↓
Recommendation / Workload
     ↓
Notification / Analytics
     ↓
Frontend
```

However, backward-compatible API changes should be preferred so services can be deployed independently.

---

# 26. Database Migration Strategy

Database changes must be handled separately from application deployment.

Flyway or Liquibase should manage migrations.

Example:

```text id="s5js3w"
V1__initial_schema.sql
V2__add_learning_goals.sql
V3__add_recommendation_factors.sql
V4__add_audit_fields.sql
```

Deployment:

```text id="fhb0jv"
Migration
   ↓
Application
```

Database migrations should generally be backward compatible with the currently running application during rolling deployments.

---

# 27. Zero-Downtime Database Changes

Avoid immediately deleting or renaming fields used by the previous application version.

Preferred approach:

```text id="z1p2wm"
Step 1
Add new column
      ↓
Step 2
Deploy application using new column
      ↓
Step 3
Migrate data
      ↓
Step 4
Stop using old column
      ↓
Step 5
Remove old column later
```

This supports rolling deployments.

---

# 28. Environment Promotion

The deployment pipeline should support:

```text id="nqf4j7"
Development
     |
     v
Testing
     |
     v
Staging
     |
     v
Production
```

A feature should not be deployed directly to production without validation.

---

# 29. Development Deployment

Development can be automatically deployed after merging into `develop`.

```text id="b3l3yd"
develop
   |
   v
CI
   |
   v
Docker Build
   |
   v
ECR
   |
   v
ECS Development
```

---

# 30. Staging Deployment

Staging should closely resemble production.

```text id="p4wz9x"
main / release
      |
      v
CI
      |
      v
ECR
      |
      v
ECS Staging
      |
      v
Smoke Tests
```

Staging should validate:

* API behavior
* Database migrations
* Kafka
* Redis
* Authentication
* Recommendation flow
* Frontend integration

---

# 31. Production Deployment

Production deployment should require an explicit approval gate for the portfolio implementation.

```text id="4h2c4u"
Release
  |
  v
CI
  |
  v
Security Checks
  |
  v
Docker Image
  |
  v
ECR
  |
  v
Approval
  |
  v
ECS Production
```

This demonstrates a controlled enterprise deployment process.

---

# 32. Smoke Testing

After deployment, automated smoke tests should verify:

```text id="0v6q3v"
Application reachable
        ↓
Authentication works
        ↓
Gateway works
        ↓
Employee API works
        ↓
Task API works
        ↓
Recommendation API works
        ↓
Database connection works
        ↓
Redis connection works
```

A deployment should be considered successful only after health checks pass.

---

# 33. Health Verification

ECS/ALB should verify:

```text id="i4un8x"
GET /actuator/health
```

Expected:

```json id="u7d9lp"
{
  "status": "UP"
}
```

If the new version fails health checks:

```text id="x8e1r2"
Deployment
   |
   X
Health Check Failure
   |
   v
Deployment Stopped / Rolled Back
```

---

# 34. Automatic Rollback

The pipeline should support rollback when:

* Health checks fail
* Error rate increases significantly
* Container crashes repeatedly
* Deployment cannot stabilize
* Critical smoke tests fail

Flow:

```text id="g4x3cq"
New Version
    |
    v
Deployment
    |
    v
Health Check
    |
    X
Failure
    |
    v
Rollback
    |
    v
Previous Stable Version
```

---

# 35. Manual Rollback

Operators should also be able to redeploy a known stable image.

Example:

```text id="6yd3i4"
Current:
recommendation-service:a81f27c

Rollback:
recommendation-service:7b42de1
```

This is another reason immutable image tags are important.

---

# 36. Monitoring After Deployment

CloudWatch should monitor:

```text id="y87x5d"
ECS CPU
ECS Memory
Task Count
ALB Requests
ALB 4xx
ALB 5xx
Response Time
RDS Health
Redis Health
Kafka Health
```

Application logs should also be reviewed after deployment.

---

# 37. Deployment Observability

The deployment should answer:

> Which version is currently running?

Example:

```text id="m5s7dk"
Service:
recommendation-service

Version:
a81f27c

Environment:
production

Deployed:
2026-09-07T12:30:00Z
```

This can be exposed through an application version endpoint or actuator information.

---

# 38. CI/CD Notifications

Pipeline status can notify the development team.

Examples:

```text id="c3l8h1"
Build Failed
Deployment Failed
Deployment Successful
Rollback Triggered
Security Scan Failed
```

Possible notification channels:

* GitHub
* Email
* Slack
* Microsoft Teams

Notification integration can be added later.

---

# 39. Pull Request Quality Gates

A PR should pass:

```text id="f0x5v1"
Compilation        ✓
Unit Tests         ✓
Integration Tests  ✓
Coverage           ✓
Static Analysis    ✓
Dependency Scan    ✓
Secret Scan        ✓
```

before merging.

---

# 40. Production Quality Gates

Before production:

```text id="d2qv3a"
All CI checks
      ↓
Security scan
      ↓
Docker scan
      ↓
Staging deployment
      ↓
Smoke tests
      ↓
Approval
      ↓
Production deployment
```

---

# 41. Example GitHub Actions CI Workflow

A simplified CI workflow:

```yaml id="4a7m3d"
name: EWSO CI

on:
  pull_request:
    branches:
      - develop
      - main

  push:
    branches:
      - develop
      - main

jobs:

  build-test:
    runs-on: ubuntu-latest

    steps:

      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Build and Test
        run: |
          mvn clean verify

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: |
            **/target/surefire-reports/**
            **/target/failsafe-reports/**
```

The exact Maven commands should be adapted to the repository's multi-module structure.

---

# 42. Example Docker Build Workflow

Conceptually:

```yaml id="8y3v8f"
name: Docker Build

on:
  push:
    branches:
      - main

jobs:

  docker:
    runs-on: ubuntu-latest

    steps:

      - name: Checkout
        uses: actions/checkout@v4

      - name: Configure AWS Credentials
        uses: aws-actions/configure-aws-credentials@v4

      - name: Login to ECR
        uses: aws-actions/amazon-ecr-login@v2

      - name: Build Docker Image
        run: |
          docker build \
            -t ewso-recommendation-service:${{ github.sha }} \
            ./backend/recommendation-service

      - name: Push Image
        run: |
          docker push \
            <ECR_REPOSITORY>:${{ github.sha }}
```

The AWS authentication section should use GitHub OIDC rather than long-lived access keys.

---

# 43. Example ECS Deployment Flow

```text id="x9c8k2"
GitHub
   |
   v
GitHub Actions
   |
   v
Build
   |
   v
Test
   |
   v
Docker Build
   |
   v
ECR
   |
   v
Update ECS Task Definition
   |
   v
ECS Service
   |
   v
Rolling Deployment
   |
   v
ALB Health Check
   |
   v
Success
```

---

# 44. Frontend CI/CD

The React application follows a similar pipeline.

```text id="e5a4a7"
React Source
    |
    v
npm ci
    |
    v
Lint
    |
    v
Unit Tests
    |
    v
Build
    |
    v
Docker Build
    |
    v
ECR
    |
    v
ECS
```

The frontend should be deployed independently from backend services where practical.

---

# 45. Backend Service CI/CD

Each backend service can follow:

```text id="p6v6jh"
Java Code
   |
   v
Maven
   |
   v
Unit Tests
   |
   v
Integration Tests
   |
   v
Security Scan
   |
   v
Docker
   |
   v
ECR
   |
   v
ECS
```

---

# 46. Monorepo Strategy

Because EWSO uses a monorepo, the CI/CD pipeline should eventually detect which services changed.

Example:

```text id="0qk2w0"
Changed:
backend/task-service/**
```

The pipeline can build:

```text id="o9q8v0"
task-service
```

without unnecessarily rebuilding every service.

However, an initial implementation can build all services for simplicity.

---

# 47. Path-Based Pipeline Optimization

Future optimization:

```text id="z3k6vw"
backend/task-service/**
        |
        v
Build task-service

backend/recommendation-service/**
        |
        v
Build recommendation-service

frontend/web-app/**
        |
        v
Build web-app
```

This reduces CI execution time.

---

# 48. Artifact Management

Build artifacts should be traceable.

Recommended identifiers:

```text id="f3a9z4"
Git Commit SHA
Docker Image Tag
Release Version
Deployment Version
```

Example:

```text id="6f2c8r"
Git:
a81f27c

Docker:
recommendation-service:a81f27c

Release:
v1.4.0
```

---

# 49. Versioning Strategy

Use semantic versioning where appropriate:

```text id="x1f8z7"
MAJOR.MINOR.PATCH
```

Example:

```text id="4h7x1y"
1.0.0
1.1.0
1.1.1
2.0.0
```

Major version:

Breaking API changes.

Minor version:

Backward-compatible functionality.

Patch:

Bug fixes.

---

# 50. API Compatibility

Microservices should prefer backward-compatible API evolution.

Example:

```text id="0x7w2p"
Old:
GET /api/v1/employees/{id}

New:
GET /api/v1/employees/{id}
```

Instead of immediately breaking clients.

Breaking changes should use a new API version when necessary:

```text id="1c7r3p"
api/v1
api/v2
```

---

# 51. Security in CI/CD

The pipeline should protect:

```text id="7b5u3a"
Source Code
Credentials
Docker Images
AWS Infrastructure
Deployment Process
```

Security controls:

* Branch protection
* Pull request reviews
* Secret scanning
* Dependency scanning
* Docker image scanning
* OIDC authentication
* Least-privilege IAM
* Protected production environment
* Audit logs

---

# 52. Branch Protection

The `main` branch should require:

* Pull request
* CI success
* Required reviews
* No direct pushes
* Required status checks

Conceptually:

```text id="3c5r1v"
Developer
    |
    X Direct main push
    |
    v
Pull Request
    |
    v
CI Checks
    |
    v
Code Review
    |
    v
Merge
```

---

# 53. Production Approval

Production should use a protected GitHub environment.

```text id="p9s4n6"
Environment:
production

Protection:
Manual Approval
```

This demonstrates an enterprise-style controlled release process.

---

# 54. Secrets by Environment

Development:

```text id="1d6w0b"
DEV_DB_PASSWORD
DEV_JWT_SECRET
```

Staging:

```text id="4a7k5r"
STAGE_DB_PASSWORD
STAGE_JWT_SECRET
```

Production:

```text id="7q3v9m"
PROD_DB_PASSWORD
PROD_JWT_SECRET
```

Production secrets should be stored in AWS Secrets Manager rather than GitHub repository files.

---

# 55. Deployment Failure Scenarios

## Scenario 1 — Tests fail

```text
Commit
  ↓
CI
  ↓
Tests FAIL
  ↓
Deployment blocked
```

## Scenario 2 — Docker scan fails

```text
Docker Build
     ↓
Security Scan
     ↓
Critical vulnerability
     ↓
Deployment blocked
```

## Scenario 3 — ECS health check fails

```text
Deployment
     ↓
Health Check
     ↓
FAIL
     ↓
Rollback
```

## Scenario 4 — Application starts but returns 500 errors

```text
Deployment
     ↓
Smoke Tests
     ↓
FAIL
     ↓
Rollback / investigation
```

---

# 56. Rollback Data Considerations

Application rollback is not enough if a database migration is incompatible.

Therefore:

```text id="2x7h1k"
Application Version
+
Database Migration
```

must be designed together.

Prefer backward-compatible migrations so that rolling back the application does not immediately break the database.

---

# 57. CI/CD for Kafka Changes

Kafka event schema changes should be backward compatible.

Example:

```text id="9k8r2m"
Old Event:
task.created

New Event:
task.created
+ additional optional field
```

Consumers should continue working during deployment.

Event contract tests should validate important event structures.

---

# 58. CI/CD for Recommendation Changes

Because recommendation logic directly affects business decisions, changes should require additional tests.

Example:

```text id="0p9t7v"
Code Change
    |
    v
Recommendation Unit Tests
    |
    v
Golden Dataset
    |
    v
Regression Test
    |
    v
CI
```

The pipeline should verify that:

* Mandatory skills remain enforced
* Critical task rules remain enforced
* Workload penalties remain functional
* Learning opportunities do not override delivery requirements
* Ranking remains deterministic for identical inputs
* Missing data is not treated as zero

---

# 59. AI CI/CD

AI-related components require additional validation.

Tests should include:

```text id="5q2n8j"
Task
 ↓
AI Analysis
 ↓
Schema Validation
 ↓
Skill Validation
 ↓
Confidence Validation
 ↓
Fallback
```

The pipeline should test:

* Invalid AI responses
* Missing fields
* Unknown skills
* Invalid complexity
* Prompt injection attempts
* Low-confidence output
* AI provider failure
* Deterministic fallback

---

# 60. AI Model/Prompt Versioning

AI behavior should be traceable.

Store:

```text id="6c9m4w"
Provider
Model
Prompt Version
Generated At
Task ID
Validation Result
Fallback Used
```

Example:

```text id="3j5r1p"
provider = external-llm
model = configured-model
promptVersion = v3
validation = PASSED
fallback = false
```

Changing a prompt should be treated as a behavior-affecting change and regression-tested.

---

# 61. Deployment Metrics

The CI/CD process should eventually measure:

### Deployment Frequency

How frequently successful deployments occur.

### Lead Time

Time from code change to production.

### Change Failure Rate

Percentage of deployments requiring rollback/fix.

### Mean Time to Recovery

Time required to restore service after a failed deployment.

These metrics can demonstrate DevOps maturity.

---

# 62. DORA-Oriented Pipeline

The project can eventually track:

```text id="7j4c1y"
Deployment Frequency
       +
Lead Time for Changes
       +
Change Failure Rate
       +
Mean Time to Recovery
```

This makes the project more representative of modern enterprise DevOps practices.

---

# 63. Complete CI/CD Pipeline

The final pipeline should look like:

```text id="q6g2u8"
                    Developer
                        |
                        v
                   Git Branch
                        |
                        v
                  Pull Request
                        |
                        v
                GitHub Actions
                        |
          +-------------+-------------+
          |                           |
          v                           v
       Compile                    Static Analysis
          |                           |
          v                           v
      Unit Tests                 Security Scan
          |                           |
          +-------------+-------------+
                        |
                        v
               Integration Tests
                        |
                        v
                  Test Coverage
                        |
                        v
                  Docker Build
                        |
                        v
                 Image Security Scan
                        |
                        v
                      ECR
                        |
                        v
                    ECS Staging
                        |
                        v
                  Smoke Testing
                        |
                        v
                 Manual Approval
                        |
                        v
                  ECS Production
                        |
                        v
                  Health Checks
                        |
             +----------+----------+
             |                     |
           PASS                   FAIL
             |                     |
             v                     v
        Deployment OK           Rollback
             |
             v
         CloudWatch
```

---

# 64. Recommended GitHub Actions Workflows

Final repository structure:

```text id="5n7b2x"
.github/
└── workflows/
    ├── ci.yml
    ├── security.yml
    ├── docker-build.yml
    ├── deploy-staging.yml
    └── deploy-production.yml
```

### `ci.yml`

Responsible for:

* Build
* Unit tests
* Integration tests
* Coverage
* Static analysis

### `security.yml`

Responsible for:

* Dependency scanning
* Secret scanning
* Security checks

### `docker-build.yml`

Responsible for:

* Docker build
* Image scan
* ECR push

### `deploy-staging.yml`

Responsible for:

* ECS staging deployment
* Smoke testing

### `deploy-production.yml`

Responsible for:

* Production approval
* ECS deployment
* Health validation
* Rollback handling

---

# 65. Definition of Done

CI/CD implementation is complete when:

* [ ] Git branching strategy is defined
* [ ] Pull requests trigger CI
* [ ] Java 21 is configured
* [ ] Maven build works
* [ ] Unit tests run automatically
* [ ] Integration tests run automatically
* [ ] Testcontainers works in CI
* [ ] JaCoCo coverage is generated
* [ ] Core business logic achieves ≥80% target coverage
* [ ] Static analysis is enabled
* [ ] Dependency scanning is enabled
* [ ] Secret scanning is enabled
* [ ] Docker images build successfully
* [ ] Docker images are scanned
* [ ] Images use immutable tags
* [ ] ECR repositories are configured
* [ ] GitHub OIDC authentication is configured
* [ ] IAM deployment role follows least privilege
* [ ] ECS deployment is automated
* [ ] Staging deployment is available
* [ ] Production requires approval
* [ ] Health checks are implemented
* [ ] Smoke tests run after deployment
* [ ] Rolling deployment is configured
* [ ] Rollback is documented/tested
* [ ] Database migrations are versioned
* [ ] Kafka event changes are tested
* [ ] AI behavior changes are regression-tested
* [ ] CloudWatch monitoring is configured
* [ ] Deployment versions are traceable
* [ ] Production secrets are externalized
* [ ] Branch protection is configured

---

# 66. Final DevOps Architecture

The complete EWSO delivery architecture is:

```text
                           GITHUB
                              |
                              v
                    +------------------+
                    | GitHub Actions   |
                    +--------+---------+
                             |
            +----------------+----------------+
            |                |                |
            v                v                v
          Build            Test           Security
            |                |                |
            +----------------+----------------+
                             |
                             v
                       Docker Build
                             |
                             v
                           ECR
                             |
                             v
                     ECS / Fargate
                             |
              +--------------+--------------+
              |              |              |
              v              v              v
          Microservices    Frontend       Workers
              |
      +-------+--------+---------+
      |                |         |
      v                v         v
     RDS            Redis      Kafka
      |                |         |
      +----------------+---------+
                       |
                       v
                  CloudWatch
```

---

# 67. Final Engineering Principles

The EWSO CI/CD architecture follows these principles:

1. **Every change is validated automatically.**
2. **Tests run before deployment.**
3. **Security checks run before production.**
4. **Docker images are immutable and traceable.**
5. **Production credentials are never stored in Git.**
6. **GitHub authenticates to AWS using temporary credentials where possible.**
7. **IAM follows least privilege.**
8. **Deployments use health checks.**
9. **Production deployments are controlled.**
10. **Rollback is always possible.**
11. **Database changes are version-controlled.**
12. **Kafka changes are backward compatible.**
13. **AI behavior is regression-tested.**
14. **Application versions are observable.**
15. **CloudWatch provides deployment/runtime visibility.**
16. **The same Docker artifact can move between environments.**

The resulting EWSO delivery model is:

> **Code → Test → Secure → Containerize → Scan → Publish → Deploy → Verify → Monitor → Rollback if necessary.**
