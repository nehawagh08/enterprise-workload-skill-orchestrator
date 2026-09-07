# AWS Architecture & Cloud Deployment

## 1. Purpose

This document defines the AWS cloud architecture for the **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)**.

The architecture maps the Dockerized EWSO application to AWS infrastructure while maintaining:

* Scalability
* High availability
* Security
* Service isolation
* Database reliability
* Event-driven communication
* Centralized monitoring
* Secret management
* CI/CD deployment
* Cost awareness

The architecture is designed so that the same Docker images developed locally can be deployed to AWS without changing application code.

---

# 2. AWS Architecture Overview

The recommended production architecture is:

```text
                           INTERNET
                              |
                              v
                    +-------------------+
                    |   Route 53 / DNS  |
                    +---------+---------+
                              |
                              v
                    +-------------------+
                    |   AWS ALB / HTTPS |
                    +---------+---------+
                              |
                              v
                    +-------------------+
                    |   API Gateway /    |
                    |   Application     |
                    |   Gateway         |
                    +---------+---------+
                              |
                    Private Application
                           Subnets
                              |
          +-------------------+-------------------+
          |                   |                   |
          v                   v                   v
   Auth Service       Employee Service     Task Service
          |                   |                   |
          +-------------------+-------------------+
                              |
                  Other Microservices
                              |
       +----------------------+----------------------+
       |                      |                      |
       v                      v                      v
     RDS                 ElastiCache              Kafka
    MySQL                   Redis               Event Stream
       |                      |                      |
       +----------------------+----------------------+
                              |
                              v
                       CloudWatch
                    Logs / Metrics / Alerts
```

Supporting AWS services:

```text
IAM
Secrets Manager
ECR
CloudWatch
VPC
Security Groups
CloudTrail
AWS Certificate Manager
```

---

# 3. AWS Service Mapping

| EWSO Component           | AWS Service                      |
| ------------------------ | -------------------------------- |
| Docker images            | Amazon ECR                       |
| Microservice containers  | Amazon ECS                       |
| Container orchestration  | ECS                              |
| External traffic         | Application Load Balancer        |
| DNS                      | Route 53                         |
| HTTPS certificates       | AWS Certificate Manager          |
| MySQL                    | Amazon RDS                       |
| Redis                    | Amazon ElastiCache               |
| Kafka                    | Amazon MSK / managed Kafka       |
| Secrets                  | AWS Secrets Manager              |
| Logs                     | CloudWatch Logs                  |
| Metrics                  | CloudWatch                       |
| IAM                      | AWS IAM                          |
| Container networking     | Amazon VPC                       |
| CI/CD                    | GitHub Actions + ECR/ECS         |
| Container image scanning | ECR scanning / CI security tools |
| Audit                    | AWS CloudTrail                   |

---

# 4. AWS VPC Architecture

EWSO should run inside a dedicated VPC.

Example:

```text
                         EWSO VPC
                            |
             +--------------+--------------+
             |                             |
       Public Subnets                Private Subnets
             |                             |
        +----+----+             +----------+----------+
        |         |             |          |           |
       ALB      NAT           ECS        RDS       ElastiCache
                              Services
                                 |
                               Kafka
```

The public internet should not directly access:

* RDS
* Redis
* Kafka
* Internal microservices

---

# 5. Availability Zones

The production architecture should use multiple Availability Zones.

Example:

```text
                   EWSO VPC
                      |
       +--------------+--------------+
       |                             |
       v                             v
Availability Zone A          Availability Zone B
       |                             |
   Public subnet                Public subnet
       |                             |
       +---------- ALB -------------+
                      |
              Private subnets
                      |
              ECS Services
                      |
              RDS / Redis / Kafka
```

Using multiple Availability Zones improves availability if one AZ experiences an infrastructure failure.

---

# 6. Subnet Design

Recommended logical subnet structure:

```text
VPC
│
├── Public Subnet A
│   └── Application Load Balancer
│
├── Public Subnet B
│   └── Application Load Balancer
│
├── Private Application Subnet A
│   └── ECS Tasks
│
├── Private Application Subnet B
│   └── ECS Tasks
│
├── Private Data Subnet A
│   ├── RDS
│   ├── Redis
│   └── Kafka
│
└── Private Data Subnet B
    ├── RDS
    ├── Redis
    └── Kafka
```

Exact CIDR ranges should be defined during infrastructure implementation.

---

# 7. Internet Gateway

The VPC requires an Internet Gateway for public internet connectivity.

Traffic flow:

```text
Internet
   |
   v
Internet Gateway
   |
   v
Public Subnet
   |
   v
Application Load Balancer
```

The ALB is the controlled public entry point.

---

# 8. NAT Gateway

Private application services may need outbound internet access for:

* External APIs
* Package downloads during controlled operations
* External AI providers
* Payment providers
* Third-party integrations

Private ECS tasks can use:

```text
Private Subnet
      |
      v
NAT Gateway
      |
      v
Internet Gateway
      |
      v
Internet
```

Inbound internet traffic should not directly reach the private services.

For a cost-conscious development environment, NAT Gateway usage should be evaluated carefully because it can become a significant recurring AWS cost.

---

# 9. Application Load Balancer

The Application Load Balancer is the public entry point for the application.

```text
Client
  |
 HTTPS
  |
  v
ALB
  |
  v
API Gateway
  |
  +--> Auth
  +--> Employee
  +--> Task
  +--> Recommendation
  +--> Workload
```

The ALB provides:

* HTTP/HTTPS routing
* Health checks
* Traffic distribution
* TLS termination
* Integration with ECS
* High availability across AZs

---

# 10. HTTPS

Production traffic should use HTTPS.

```text
Client
   |
 HTTPS :443
   |
   v
ALB
```

AWS Certificate Manager can manage the TLS certificate.

HTTP requests can be redirected to HTTPS.

---

# 11. ECS Container Deployment

The Dockerized Spring Boot services should run as ECS tasks.

Example:

```text
Amazon ECS
│
├── API Gateway Service
├── Auth Service
├── Employee Service
├── Skill Service
├── Project Service
├── Task Service
├── Workload Service
├── Recommendation Service
├── Performance Service
├── Notification Service
└── Analytics Service
```

Each service can have an independent ECS service.

This allows services to scale independently.

---

# 12. ECS Service Model

For example:

```text
recommendation-service
        |
        +--- Task 1
        |
        +--- Task 2
        |
        +--- Task 3
```

If recommendation traffic increases:

```text
3 tasks → 5 tasks
```

without scaling unrelated services.

This is an important advantage of the microservice architecture.

---

# 13. ECS Launch Strategy

For the portfolio implementation, **ECS with Fargate** is a practical starting point.

Fargate removes the need to manage EC2 servers directly.

Conceptually:

```text
Docker Image
     |
     v
Amazon ECR
     |
     v
ECS Fargate
     |
     +--> Container
     +--> Container
     +--> Container
```

For a larger enterprise implementation, EC2-backed ECS or EKS could also be evaluated.

---

# 14. Amazon ECR

Amazon Elastic Container Registry stores Docker images.

Example:

```text
ECR
│
├── ewso/api-gateway
├── ewso/auth-service
├── ewso/employee-service
├── ewso/skill-service
├── ewso/project-service
├── ewso/task-service
├── ewso/workload-service
├── ewso/recommendation-service
├── ewso/performance-service
├── ewso/notification-service
└── ewso/analytics-service
```

CI/CD builds and pushes images to ECR.

---

# 15. Image Deployment Flow

```text
Developer
    |
    v
GitHub
    |
    v
GitHub Actions
    |
    +--> Maven Build
    |
    +--> Unit Tests
    |
    +--> Integration Tests
    |
    +--> Security Scan
    |
    v
Docker Build
    |
    v
Amazon ECR
    |
    v
ECS Deployment
```

---

# 16. RDS MySQL

EWSO's transactional data should use Amazon RDS for MySQL.

```text
ECS Services
      |
      v
Amazon RDS
      |
      v
MySQL
```

RDS provides managed:

* Database infrastructure
* Automated backups
* Monitoring
* Maintenance
* Multi-AZ options
* Storage management

---

# 17. Database Ownership

The microservice architecture should preserve logical database ownership.

Example:

```text
employee-service
       |
       v
RDS
 └── ewso_employee
```

```text
task-service
       |
       v
RDS
 └── ewso_task
```

```text
recommendation-service
       |
       v
RDS
 └── ewso_recommendation
```

In the initial implementation, these may exist as separate schemas within one RDS instance.

For a larger production environment, database isolation can be increased as required.

---

# 18. RDS Security

RDS should be located in private subnets.

It should not have a public IP.

Traffic should be restricted through security groups.

Example:

```text
ECS Security Group
       |
       | TCP 3306
       v
RDS Security Group
```

Only authorized application services should be able to connect.

---

# 19. RDS Backup Strategy

Production RDS should use:

* Automated backups
* Backup retention
* Point-in-time recovery
* Multi-AZ where required
* Monitoring
* Storage encryption

Backup retention should be defined according to business requirements.

---

# 20. ElastiCache Redis

Redis is used for caching frequently accessed data.

```text
ECS Services
      |
      v
ElastiCache Redis
```

Example cached data:

```text
employee:{id}:skills
employee:{id}:availability
employee:{id}:workload
employee:{id}:performance
recommendation:{taskId}
skills:catalogue
```

Redis is not the source of truth.

---

# 21. Redis Architecture

Example:

```text
                ECS
                 |
                 v
         ElastiCache Redis
                 |
       +---------+---------+
       |                   |
   Skill Cache        Workload Cache
```

Production configuration can use replication and appropriate failover capabilities.

---

# 22. Kafka on AWS

Kafka provides asynchronous communication between services.

For AWS, managed Kafka can be provided through **Amazon MSK**.

Architecture:

```text
ECS Services
     |
     v
Amazon MSK
     |
     +--> task-events
     +--> employee-events
     +--> skill-events
     +--> recommendation-events
     +--> performance-events
     +--> notification-events
```

---

# 23. Kafka Event Flow

Example task creation:

```text
User
 |
 v
Task Service
 |
 +--> MySQL
 |
 +--> Kafka
        |
        v
   task.created
        |
        +--> Recommendation Service
        |
        +--> Notification Service
        |
        +--> Analytics Service
```

This prevents every downstream operation from needing to happen synchronously.

---

# 24. Recommendation Architecture on AWS

The recommendation engine is deployed as an independent ECS service.

```text
                    Task Service
                         |
                         v
                Recommendation Service
                         |
       +-----------------+-----------------+
       |                 |                 |
       v                 v                 v
 Employee Service    Workload Service   Performance
       |                 |                 |
       +-----------------+-----------------+
                         |
                         v
                       Redis
                         |
                         v
                       MySQL
```

This service remains independently scalable.

---

# 25. AI Task Intelligence

The AI component can initially run within the application architecture.

Future architecture:

```text
Task Service
     |
     v
AI Task Intelligence Service
     |
     +--> LLM Provider
     |
     +--> Skill Extraction
     |
     +--> Classification
     |
     +--> Complexity
     |
     v
Structured Task Understanding
     |
     v
Recommendation Service
```

The external AI provider should never become the authority for employee assignment.

---

# 26. AI Secrets

AI provider credentials must be stored in:

```text
AWS Secrets Manager
```

not:

```text
Dockerfile
GitHub repository
application.yml
source code
```

The ECS task retrieves the secret through IAM-controlled access.

---

# 27. AWS Secrets Manager

Secrets can include:

```text
MYSQL_USERNAME
MYSQL_PASSWORD
JWT_SECRET
PAYPAL_CLIENT_SECRET
LLM_API_KEY
KAFKA_CREDENTIALS
```

The application receives these at runtime.

Architecture:

```text
ECS Task
   |
   | IAM permission
   v
Secrets Manager
   |
   v
Secret
```

---

# 28. IAM Architecture

AWS IAM should follow least privilege.

Example roles:

```text
ECS Task Role
   |
   +--> Read required secrets
   +--> Write CloudWatch logs
   +--> Access required AWS services
```

The task should not have unrestricted AWS permissions.

For example, a recommendation service should not automatically receive:

```text
AdministratorAccess
```

---

# 29. Security Group Design

Recommended security groups:

```text
ALB-SG
   |
   +--> HTTPS 443 from Internet
   |
   v
ECS-SG
   |
   +--> Application ports
   |
   +--> RDS
   +--> Redis
   +--> Kafka
```

RDS should allow database traffic only from authorized ECS security groups.

Redis should allow traffic only from authorized application services.

Kafka should allow traffic only from authorized application services.

---

# 30. Network Security

The production architecture should enforce:

* Private database subnets
* Private Redis
* Private Kafka
* Restricted security groups
* HTTPS
* IAM
* Secrets Manager
* Encryption at rest
* Encryption in transit where supported
* No unnecessary public ports

The following should not be publicly exposed:

```text
3306 MySQL
6379 Redis
9092 Kafka
```

---

# 31. CloudWatch Logging

All application containers should send logs to CloudWatch.

Example:

```text
ECS
 |
 +--> API Gateway Logs
 +--> Auth Logs
 +--> Employee Logs
 +--> Task Logs
 +--> Recommendation Logs
 +--> Workload Logs
 +--> Notification Logs
 |
 v
CloudWatch Logs
```

Logs should include:

* Timestamp
* Service name
* Log level
* Correlation ID
* Request information
* Error code
* Exception information where appropriate

---

# 32. Application Monitoring

CloudWatch can monitor:

* CPU utilization
* Memory utilization
* ECS task count
* Request count
* ALB response time
* HTTP 4xx
* HTTP 5xx
* RDS metrics
* Redis metrics
* Kafka metrics

Example alert:

```text
Recommendation Service
CPU > 80%
        |
        v
CloudWatch Alarm
        |
        v
Scaling / Notification
```

---

# 33. ECS Auto Scaling

ECS services should support horizontal scaling.

Example:

```text
Normal traffic
     |
     v
2 tasks


High traffic
     |
     v
4 tasks


Very high traffic
     |
     v
8 tasks
```

Scaling can be based on:

* CPU
* Memory
* Request count
* Target response metrics
* Custom application metrics

---

# 34. Independent Service Scaling

One of the major benefits of the architecture is independent scaling.

Example:

```text
Task Service
2 instances

Recommendation Service
6 instances

Analytics Service
2 instances
```

If recommendation requests increase, only the recommendation service needs to scale.

---

# 35. Availability Strategy

Critical services should run with more than one task where appropriate.

Example:

```text
                 ALB
                  |
        +---------+---------+
        |                   |
        v                   v
 Recommendation Task 1  Recommendation Task 2
        |                   |
        +---------+---------+
                  |
                Redis
                  |
                 RDS
```

If one task fails, traffic can be routed to another healthy task.

---

# 36. Service Health Checks

Each service should expose:

```text
/actuator/health
```

ECS/ALB can use health checks to determine whether a task is healthy.

Example:

```text
ALB
 |
 +--> Task 1 → HEALTHY
 |
 +--> Task 2 → UNHEALTHY
 |
 +--> Task 3 → HEALTHY
```

Traffic should not be sent to unhealthy instances.

---

# 37. Deployment Strategy

The preferred deployment approach is rolling deployment.

Example:

```text
Version 1
Task 1
Task 2

        ↓ Deployment

Version 1       Version 2
Task 1          Task 3
Task 2          Task 4

        ↓

Version 2
Task 3
Task 4
```

This reduces downtime.

---

# 38. Rollback

If a deployment introduces a critical problem:

```text
Version 2
   |
   v
Health checks fail
   |
   v
Deployment stopped
   |
   v
Previous stable version
```

Container image versioning makes rollback easier.

Example:

```text
recommendation-service:1.4.0
recommendation-service:1.3.0
```

---

# 39. CI/CD Architecture

The deployment pipeline should be:

```text
Developer
    |
    v
Git Push
    |
    v
GitHub Actions
    |
    +--> Compile
    |
    +--> Unit Tests
    |
    +--> Integration Tests
    |
    +--> Coverage
    |
    +--> Static Analysis
    |
    +--> Dependency Scan
    |
    +--> Docker Build
    |
    +--> Image Scan
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

The complete CI/CD implementation will be documented separately in:

```text
docs/09-deployment/ci-cd.md
```

---

# 40. Environment Strategy

The project should have separate environments.

```text
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

Each environment should have independent:

* Configuration
* Secrets
* Databases
* Redis
* Kafka resources where appropriate
* ECS services

---

# 41. Configuration Management

Application code should remain environment-independent.

For example:

```text
Local:
MYSQL_HOST=mysql

AWS:
MYSQL_HOST=<RDS endpoint>
```

The application code remains unchanged.

Only configuration changes.

---

# 42. AWS Resource Naming

A consistent naming convention should be used.

Examples:

```text
ewso-vpc
ewso-alb
ewso-cluster
ewso-auth-service
ewso-employee-service
ewso-task-service
ewso-recommendation-service
ewso-rds
ewso-redis
ewso-msk
ewso-secrets
```

Environment prefixes can be added:

```text
ewso-dev-*
ewso-stage-*
ewso-prod-*
```

---

# 43. Data Encryption

Sensitive data should be encrypted:

```text
Client
  |
 HTTPS
  |
  v
ALB
  |
 TLS
  |
  v
Application
  |
  +--> Encrypted RDS
  |
  +--> Encrypted Redis
  |
  +--> Encrypted Kafka
```

AWS-managed encryption mechanisms should be enabled where applicable.

---

# 44. Audit Logging

Security-sensitive operations should be auditable.

EWSO application audit logs include:

* Login events
* Permission changes
* Task assignment
* Task reassignment
* Recommendation acceptance
* Recommendation rejection
* Manager override
* Employee profile changes
* Skill changes

AWS CloudTrail should additionally capture relevant AWS API activity.

---

# 45. Disaster Recovery

The architecture should account for failures involving:

```text
Application
Database
Cache
Messaging
Availability Zone
Deployment
```

Recovery mechanisms include:

* RDS backups
* Point-in-time recovery
* Multi-AZ configuration where appropriate
* Container replacement
* ECS health checks
* Image versioning
* Infrastructure-as-code
* Centralized configuration
* Documented rollback procedures

---

# 46. Failure Example — Recommendation Service

If one recommendation task fails:

```text
Request
   |
   v
Recommendation Service
   |
   X Failure
   |
   v
Circuit Breaker / Error Handling
   |
   v
Manager Review
```

The system should not silently assign an unsuitable employee.

---

# 47. Failure Example — Redis

If Redis fails:

```text
Recommendation Service
       |
       X
     Redis
       |
       v
Fallback
       |
       v
Required service/database
```

Redis is an optimization layer, not the source of truth.

---

# 48. Failure Example — Kafka

If Kafka becomes temporarily unavailable:

```text
Application
     |
     v
Kafka unavailable
     |
     v
Retry / controlled failure handling
     |
     v
Business operation protected
```

Critical synchronous operations should not be unnecessarily blocked by asynchronous infrastructure.

---

# 49. Failure Example — AI Provider

If the external AI provider fails:

```text
Task
 |
 v
AI Task Intelligence
 |
 X AI unavailable
 |
 v
Deterministic Fallback
 |
 v
Recommendation Engine
 |
 v
Manager
```

The system remains usable.

This is particularly important because AI is an enhancement layer rather than the core decision authority.

---

# 50. AWS Architecture for Knowledge Intelligence

The future Knowledge Service can use:

```text
Jira
GitHub
Architecture Docs
Incident Reports
API Documentation
      |
      v
Knowledge Ingestion
      |
      v
Chunking / Processing
      |
      v
Embeddings
      |
      v
Vector Database
      |
      v
RAG Retrieval
      |
      v
Knowledge Assistant
```

Potential AWS services and supporting technologies can be evaluated later.

This is intentionally outside the initial MVP deployment.

---

# 51. Cost-Aware Portfolio Deployment

A full enterprise AWS architecture can become expensive.

For the portfolio implementation, the recommended approach is to start smaller.

### Portfolio MVP

```text
ALB
 |
ECS Fargate
 |
+--> Spring Boot Services
 |
RDS MySQL
 |
ElastiCache Redis
```

Kafka can initially run locally through Docker Compose.

Then migrate to managed Kafka when the event-driven architecture is demonstrated.

This allows the project to demonstrate AWS skills without unnecessarily creating a large monthly bill.

---

# 52. Recommended AWS Implementation Phases

## Phase 1 — Basic Cloud Deployment

Deploy:

```text
ECR
ECS
RDS
ALB
CloudWatch
IAM
Secrets Manager
```

Goal:

```text
React
  ↓
ALB
  ↓
ECS
  ↓
RDS
```

---

## Phase 2 — Caching

Add:

```text
ElastiCache Redis
```

Architecture:

```text
ECS
 |
 +--> RDS
 |
 +--> Redis
```

---

## Phase 3 — Event-Driven Architecture

Add:

```text
Amazon MSK
```

Architecture:

```text
ECS
 |
 v
Kafka
 |
 +--> Recommendation
 +--> Notification
 +--> Analytics
```

---

## Phase 4 — Production Hardening

Add:

```text
Multi-AZ
Auto Scaling
CloudWatch Alarms
Security Hardening
Backup Strategy
Deployment Rollback
```

---

## Phase 5 — AI

Add:

```text
AI Task Intelligence
        |
        v
External LLM / AI Provider
```

with:

* Secrets Manager
* timeout
* retry
* fallback
* monitoring
* prompt/version tracking

---

## Phase 6 — Knowledge Intelligence

Future:

```text
Jira / GitHub / Docs
        |
        v
Knowledge Service
        |
        v
Vector Database
        |
        v
RAG Assistant
```

---

# 53. AWS Architecture — Complete View

The final target architecture is:

```text
                               INTERNET
                                   |
                                   v
                           +---------------+
                           |    Route 53   |
                           +-------+-------+
                                   |
                                   v
                           +---------------+
                           |      ALB      |
                           | HTTPS / TLS   |
                           +-------+-------+
                                   |
                                   v
                         +-------------------+
                         |   ECS / Fargate   |
                         | Private Subnets   |
                         +---------+---------+
                                   |
        +------------+-------------+-------------+-------------+
        |            |             |             |             |
        v            v             v             v             v
      Auth       Employee        Task       Recommendation  Analytics
        |            |             |             |             |
        +------------+-------------+-------------+-------------+
                                   |
                   +---------------+---------------+
                   |               |               |
                   v               v               v
                  RDS           Redis             MSK
                 MySQL        ElastiCache         Kafka
                   |               |               |
                   +---------------+---------------+
                                   |
                                   v
                              CloudWatch

Supporting:
IAM
Secrets Manager
ECR
CloudTrail
VPC
Security Groups
Certificate Manager
```

---

# 54. Security Architecture Summary

```text
Internet
   |
 HTTPS
   |
 ALB
   |
 Private ECS
   |
 +-------+--------+--------+
 |       |        |        |
 RDS   Redis    Kafka    Services
 |       |        |
 +-------+--------+
         |
   Private Network
```

Security principles:

1. Public access only through controlled entry points.
2. Backend services run in private subnets.
3. Databases are private.
4. Redis is private.
5. Kafka is private.
6. Secrets are externalized.
7. IAM follows least privilege.
8. HTTPS is mandatory in production.
9. Security groups restrict communication.
10. Audit logs are retained.
11. Container images are scanned.
12. Application authorization remains inside the services.

---

# 55. AWS Definition of Done

AWS deployment is considered complete when:

* [ ] AWS VPC is configured
* [ ] Public/private subnet architecture is implemented
* [ ] Security groups are configured
* [ ] ECR repositories are created
* [ ] Docker images can be pushed to ECR
* [ ] ECS cluster is created
* [ ] ECS services are deployed
* [ ] ALB is configured
* [ ] HTTPS is configured
* [ ] Route 53 is configured if a domain is used
* [ ] RDS MySQL is deployed
* [ ] Database backups are configured
* [ ] Redis is deployed
* [ ] Kafka/MSK is configured
* [ ] Secrets Manager is configured
* [ ] IAM roles follow least privilege
* [ ] CloudWatch logs are enabled
* [ ] CloudWatch metrics/alarms are configured
* [ ] ECS health checks are working
* [ ] Auto scaling is configured where required
* [ ] Rolling deployments are tested
* [ ] Rollback is tested
* [ ] Security scanning is integrated
* [ ] CI/CD deployment is automated
* [ ] Production configuration is externalized
* [ ] Disaster recovery procedures are documented
* [ ] AWS costs are monitored

---

# 56. Key Architectural Decisions

### Decision 1 — ECS over manually managed servers

ECS/Fargate reduces infrastructure management while still demonstrating containerized microservice deployment.

### Decision 2 — RDS instead of MySQL container in production

RDS provides managed database operations, backups, monitoring, and high-availability options.

### Decision 3 — ElastiCache instead of Redis container in production

Caching infrastructure becomes independently managed from application containers.

### Decision 4 — Managed Kafka for enterprise deployment

Kafka remains the event backbone while AWS manages much of the infrastructure through MSK.

### Decision 5 — Secrets Manager instead of environment-file secrets

Sensitive credentials should not be stored in source control or Docker images.

### Decision 6 — AI remains advisory

The deterministic recommendation engine remains the authoritative business decision layer.

### Decision 7 — Independent ECS services

Each microservice can scale and deploy independently.

---

# 57. Final Deployment Principle

The EWSO cloud architecture follows:

```text
                 BUSINESS APPLICATION
                         |
                         v
              Spring Boot Microservices
                         |
                         v
                  Docker Containers
                         |
                         v
                    Amazon ECS
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
         RDS          Redis/MSK      External AI
          |              |              |
          +--------------+--------------+
                         |
                         v
                    CloudWatch
```

The architecture separates:

* Application logic
* Infrastructure
* Data
* Messaging
* Caching
* AI
* Security
* Observability

This allows EWSO to evolve from a portfolio project into a realistic enterprise-style platform without requiring a redesign of its core business architecture.
