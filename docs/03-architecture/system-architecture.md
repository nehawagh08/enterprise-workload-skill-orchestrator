# System Architecture

**Project:** AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)
**Document:** System Architecture
**Version:** 1.0
**Status:** Draft

---

# 1. Purpose

This document defines the high-level technical architecture of the Enterprise Workload & Skill Orchestrator (EWSO).

The architecture is designed to support:

* Intelligent task allocation
* Employee skill management
* Workload optimization
* Explainable recommendations
* Event-driven processing
* Employee development
* Workforce analytics
* Enterprise security
* Independent service scaling
* Future AI and knowledge intelligence

The system will initially follow a **microservices-oriented architecture** using Java and Spring Boot.

---

# 2. Architecture Goals

The architecture shall provide:

1. Clear service ownership
2. Independent service deployment
3. Horizontal scalability
4. Secure API access
5. Asynchronous event processing
6. Reliable data management
7. Explainable recommendation logic
8. Centralized observability
9. Easy automated testing
10. Future integration with AI/LLM systems
11. Future integration with Jira, GitHub, and ServiceNow
12. Cloud deployment capability

---

# 3. High-Level Architecture

The overall architecture is:

```text
                         ┌───────────────────────┐
                         │       End Users       │
                         │                       │
                         │ Employee / Manager    │
                         │ Team Lead / Admin     │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │    React Frontend     │
                         │    TypeScript         │
                         └───────────┬───────────┘
                                     │ HTTPS
                                     ▼
                         ┌───────────────────────┐
                         │      API Gateway      │
                         │   Spring Cloud        │
                         └───────────┬───────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
              ▼                      ▼                      ▼
       ┌────────────┐         ┌────────────┐        ┌────────────┐
       │Auth Service│         │Employee    │        │Skill       │
       │            │         │Service     │        │Service     │
       └────────────┘         └────────────┘        └────────────┘
              │                      │                      │
              │                      │                      │
              └──────────────┬───────┴──────────────┬───────┘
                             │                      │
                             ▼                      ▼
                       ┌────────────┐        ┌───────────────┐
                       │Task        │        │Workload       │
                       │Service     │        │Service        │
                       └─────┬──────┘        └───────┬───────┘
                             │                       │
                             ▼                       │
                  ┌──────────────────────┐           │
                  │ Recommendation       │◄──────────┘
                  │ Service              │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │ AI Task Intelligence │
                  │                     │
                  │ Classification      │
                  │ Skill Extraction     │
                  │ Complexity Analysis  │
                  └──────────────────────┘

                             │
                             ▼
                    ┌─────────────────┐
                    │     Kafka       │
                    │ Event Streaming │
                    └───────┬─────────┘
                            │
              ┌─────────────┼──────────────┐
              ▼             ▼              ▼
       Notification     Analytics      Performance
         Service          Service         Service


       ┌─────────────────────────────────────────┐
       │              Infrastructure             │
       │                                         │
       │ MySQL    Redis    Kafka    Monitoring  │
       └─────────────────────────────────────────┘
```

---

# 4. Architectural Style

EWSO will use a combination of:

### 4.1 Microservices Architecture

Business capabilities are separated into independently deployable services.

### 4.2 REST Architecture

Synchronous communication between services will primarily use REST APIs.

### 4.3 Event-Driven Architecture

Kafka will be used for asynchronous business events.

### 4.4 Layered Architecture

Each Spring Boot service will internally follow:

```text
Controller
    ↓
Service
    ↓
Domain / Business Logic
    ↓
Repository
    ↓
Database
```

### 4.5 Domain-Oriented Service Boundaries

Services should own a clearly defined business capability rather than being divided arbitrarily by database tables.

---

# 5. Frontend Layer

## Technology

* React
* TypeScript
* HTML
* CSS
* REST API client

The frontend will provide role-specific dashboards.

---

# 6. React Application Responsibilities

The frontend shall handle:

* Authentication UI
* Employee dashboard
* Manager dashboard
* Task management
* Recommendation display
* Workload visualization
* Skill management
* Learning goals
* Notifications
* Workforce analytics

The frontend shall not contain core business decision logic.

For example, recommendation scoring must remain on the backend.

---

# 7. API Gateway

## Service

`api-gateway`

## Technology

Spring Cloud Gateway

The API Gateway acts as the single entry point for frontend requests.

```text
React
  ↓
API Gateway
  ↓
Backend Services
```

---

# 8. API Gateway Responsibilities

The gateway shall handle:

* Request routing
* Authentication token validation
* Authorization checks where appropriate
* Rate limiting
* Request logging
* Correlation ID propagation
* CORS
* Centralized API entry point

Example:

```text
GET /api/employees
        ↓
API Gateway
        ↓
Employee Service
```

---

# 9. Service Registry

## Service

`service-registry`

## Technology

Spring Cloud Netflix Eureka

The service registry allows services to discover each other dynamically.

Example:

```text
Recommendation Service
        ↓
Eureka
        ↓
Employee Service
```

The recommendation service does not need to hardcode the employee-service host.

---

# 10. Auth Service

## Service

`auth-service`

## Responsibilities

* User registration
* Login
* JWT generation
* Password hashing
* Role management
* Authentication-related operations

Supported roles:

```text
ADMIN
MANAGER
TEAM_LEAD
PROJECT_MANAGER
EMPLOYEE
```

---

# 11. Employee Service

## Service

`employee-service`

## Responsibilities

* Employee profile
* Department
* Designation
* Experience
* Manager relationship
* Availability
* Employee search

Example APIs:

```text
POST   /api/employees
GET    /api/employees/{id}
GET    /api/employees
PUT    /api/employees/{id}
```

---

# 12. Skill Service

## Service

`skill-service`

## Responsibilities

* Skill catalogue
* Employee skills
* Proficiency
* Experience
* Skill history
* Certifications
* Learning goals

Example:

```text
Employee
   ↓
Java — Advanced
Spring Boot — Advanced
Kafka — Intermediate
AWS — Beginner
```

---

# 13. Project Service

## Service

`project-service`

## Responsibilities

* Project creation
* Project metadata
* Project manager
* Project status
* Project skill requirements
* Project team information

Example:

```text
Project
   ↓
Required Skills
   ├── Java
   ├── Spring Boot
   ├── Kafka
   └── AWS
```

---

# 14. Task Service

## Service

`task-service`

## Responsibilities

* Task creation
* Task updates
* Task lifecycle
* Priority
* Complexity
* Deadline
* Estimated effort
* Required skills
* Assignment

Task lifecycle:

```text
CREATED
   ↓
RECOMMENDATION_GENERATED
   ↓
ASSIGNED
   ↓
IN_PROGRESS
   ↓
COMPLETED
```

Additional states:

```text
BLOCKED
CANCELLED
REOPENED
```

---

# 15. Workload Service

## Service

`workload-service`

## Responsibilities

* Employee capacity
* Assigned effort
* Workload calculation
* Workload status
* Team utilization
* Capacity analysis

Formula:

```text
Workload % =
Assigned Hours / Available Hours × 100
```

Example:

```text
Capacity = 40 hours

Assigned = 28 hours

Workload = 70%
```

---

# 16. Recommendation Service

## Service

`recommendation-service`

This is the **core intelligence service** of EWSO.

Responsibilities:

* Candidate eligibility
* Skill matching
* Workload evaluation
* Availability evaluation
* Experience evaluation
* Performance evaluation
* Growth opportunity evaluation
* Candidate scoring
* Ranking
* Explanation generation

---

# 17. Recommendation Architecture

```text
                Task
                 │
                 ▼
       ┌────────────────────┐
       │ Eligibility Engine │
       └─────────┬──────────┘
                 │
                 ▼
          Candidate Pool
                 │
       ┌─────────┼──────────┐
       ▼         ▼          ▼
    Skills    Workload   Availability
       │         │          │
       └─────────┼──────────┘
                 ▼
          Scoring Engine
                 │
                 ▼
          Ranking Engine
                 │
                 ▼
       Explanation Builder
                 │
                 ▼
       Recommendation Result
```

---

# 18. Recommendation Scoring

Initial scoring:

```text
Skill Match           30%
Experience            20%
Workload              20%
Task Complexity       10%
Availability          10%
Performance            5%
Learning Opportunity   5%
```

Total:

```text
100%
```

The scoring weights shall be configuration-driven.

---

# 19. AI Task Intelligence

## Component

`ai-engine`

The AI layer shall analyze task information.

Initial capabilities:

* Skill extraction
* Task classification
* Complexity estimation
* Technical-domain detection
* Task summarization

Example:

```text
Input:

"Fix Redis timeout issue in Spring Boot
payment service."

        ↓

AI Task Intelligence

        ↓

Skills:
Java
Spring Boot
Redis
Microservices

Complexity:
Medium

Domain:
Backend / Payments
```

---

# 20. AI Architecture Principle

The AI layer shall **enhance business logic rather than replace it**.

```text
AI
 ↓
Understands Task

Business Rules
 ↓
Determine Eligibility

Scoring Engine
 ↓
Calculate Recommendation
```

This separation improves:

* Explainability
* Reliability
* Testing
* Debugging
* Vendor independence

---

# 21. Performance Service

## Service

`performance-service`

## Responsibilities

Track:

* Task completion
* On-time percentage
* Average completion time
* Reopened tasks
* Incident resolution
* Quality score

Performance information may be consumed by the recommendation engine.

---

# 22. Notification Service

## Service

`notification-service`

Responsibilities:

* Task assignment notifications
* Deadline notifications
* Overdue notifications
* Manager alerts
* Skill-gap notifications

Notifications should primarily be triggered asynchronously through Kafka events.

---

# 23. Analytics Service

## Service

`analytics-service`

Responsibilities:

* Team utilization
* Workload distribution
* Task statistics
* Skill availability
* Skill gaps
* Project capacity
* Workforce trends

Analytics should avoid placing heavy analytical queries directly on transactional services.

---

# 24. Knowledge Service

## Service

`knowledge-service`

**Version 2 / Future**

The knowledge service will eventually support:

* Jira ingestion
* GitHub ingestion
* Project documents
* Architecture documents
* Incident reports
* API documentation
* Embeddings
* Vector search
* RAG-based enterprise Q&A

---

# 25. Service Communication

EWSO uses two communication patterns.

## 25.1 Synchronous

REST/OpenFeign.

Used when an immediate response is required.

Example:

```text
Recommendation Service
        ↓
Employee Service
        ↓
Employee Data
```

---

## 25.2 Asynchronous

Kafka.

Used for events that do not require an immediate response.

Example:

```text
Task Service
     ↓
task.assigned
     ↓
Kafka
     ├── Workload Service
     ├── Notification Service
     ├── Analytics Service
     └── Audit
```

---

# 26. Why Both REST and Kafka?

REST is useful for:

```text
Request → Response
```

Kafka is useful for:

```text
Event → Multiple Consumers
```

Example:

When a task is assigned:

```text
task.assigned
```

multiple services need to react.

Using Kafka avoids tightly coupling Task Service to every downstream service.

---

# 27. Kafka Architecture

Kafka will act as the event backbone.

Example topics:

```text
task-events
employee-events
skill-events
recommendation-events
performance-events
notification-events
```

Possible events:

```text
task.created
task.assigned
task.completed
task.blocked

employee.availability.changed
employee.skill.updated

recommendation.generated

performance.updated
```

---

# 28. Kafka Consumer Model

Example:

```text
                Kafka
                  │
           task.assigned
                  │
       ┌──────────┼───────────┐
       ▼          ▼           ▼
   Workload   Notification  Analytics
   Consumer     Consumer     Consumer
```

Each consumer performs its own responsibility.

---

# 29. Idempotency

Kafka events may occasionally be delivered more than once.

Consumers must therefore be idempotent.

Example:

```text
task.assigned
task.assigned
```

must not result in:

```text
Workload + 8 hours
Workload + 8 hours
```

if the event represents the same assignment.

The system should maintain an event identifier or equivalent deduplication mechanism.

---

# 30. Redis Architecture

Redis will provide caching.

Potential cached information:

```text
employee:{id}:skills
employee:{id}:availability
team:{id}:workload
recommendation:{taskId}
skills:catalogue
```

Example:

```text
Recommendation Request
        ↓
      Redis
        ↓
   Cache Hit?
      /    \
    Yes     No
     ↓       ↓
  Return   MySQL
             ↓
           Redis
```

---

# 31. Database Architecture

The initial system will use MySQL.

Each service should conceptually own its business data.

Example:

```text
Auth Service
   ↓
Auth Database

Employee Service
   ↓
Employee Database

Skill Service
   ↓
Skill Database

Task Service
   ↓
Task Database
```

For the initial portfolio implementation, these may be implemented as separate schemas/databases within one MySQL deployment.

Future production deployment may use independently managed databases.

---

# 32. Database Ownership Principle

A service should not directly modify another service's database.

Incorrect:

```text
Task Service
    ↓
UPDATE employee_database.employee
```

Correct:

```text
Task Service
    ↓
Employee Service API
```

or:

```text
Task Service
    ↓
Kafka Event
    ↓
Employee-related consumer
```

This preserves service boundaries.

---

# 33. Transaction Boundaries

Each service controls its own local transactions.

Example:

```text
Task Service

Create Task
   ↓
Save Task
   ↓
Commit
   ↓
Publish task.created
```

Cross-service distributed transactions should be avoided where possible.

---

# 34. Eventual Consistency

Because the system uses asynchronous events, some information may temporarily be eventually consistent.

Example:

```text
Task Assigned
      ↓
Task Service updated immediately
      ↓
Kafka Event
      ↓
Workload Service processes event
      ↓
Workload updated
```

There may be a small delay between assignment and workload dashboard update.

This is an accepted architectural trade-off.

---

# 35. API Request Flow

Example:

```text
Manager
   ↓
React
   ↓
POST /api/recommendations/tasks/TASK-1024
   ↓
API Gateway
   ↓
Recommendation Service
   ↓
Employee Service
   ↓
Skill Service
   ↓
Workload Service
   ↓
Scoring Engine
   ↓
Ranking Engine
   ↓
Explanation Builder
   ↓
Response
   ↓
React
```

---

# 36. Task Assignment Flow

```text
Manager
   ↓
Create Task
   ↓
Task Service
   ↓
task.created
   ↓
Kafka
   ↓
Recommendation processing
   ↓
Candidate Ranking
   ↓
Manager reviews recommendations
   ↓
Select Employee
   ↓
Task Service
   ↓
task.assigned
   ↓
Kafka
   ├── Workload Service
   ├── Notification Service
   ├── Analytics Service
   └── Audit
```

---

# 37. Recommendation Data Flow

```text
Task
 │
 ├── Required Skills
 ├── Complexity
 ├── Priority
 ├── Deadline
 └── Estimated Effort
          │
          ▼
   Recommendation Service
          │
          ├──────────────┐
          ▼              ▼
   Employee Service   Skill Service
          │              │
          └──────┬───────┘
                 ▼
          Workload Service
                 │
                 ▼
          Performance Data
                 │
                 ▼
          Learning Goals
                 │
                 ▼
          Scoring Engine
                 │
                 ▼
          Ranking Engine
                 │
                 ▼
          Explanation
```

---

# 38. Security Architecture

```text
React
  │
 HTTPS
  ↓
API Gateway
  │
 JWT Validation
  ↓
Backend Services
```

Security mechanisms:

* JWT
* Spring Security
* RBAC
* Password hashing
* Input validation
* API authorization
* Secure secrets
* Audit logging
* HTTPS in deployed environments

---

# 39. Error Handling Architecture

All services shall follow a consistent error-response structure.

Example:

```json
{
  "timestamp": "2026-09-07T10:30:00Z",
  "status": 404,
  "code": "EMPLOYEE_NOT_FOUND",
  "message": "Employee was not found",
  "path": "/api/employees/101",
  "correlationId": "REQ-82A71"
}
```

Services should use centralized exception handling within each service.

---

# 40. Resilience Architecture

Inter-service communication should support:

```text
Timeout
Retry
Circuit Breaker
Fallback
```

Example:

```text
Recommendation Service
       │
       ▼
Employee Service
       │
       X
   Failure
       │
       ▼
Circuit Breaker
       │
       ▼
Controlled Failure
```

---

# 41. Observability Architecture

The system should support:

```text
Application Logs
       +
Metrics
       +
Health Checks
       +
Correlation IDs
       +
Distributed Tracing
```

Future cloud deployment may use AWS CloudWatch and distributed tracing tools.

---

# 42. Logging Flow

```text
Request
   ↓
API Gateway
   ↓
Correlation ID
   ↓
Service A
   ↓
Service B
   ↓
Service C
```

Every service should include the correlation ID in relevant logs.

This allows engineers to trace a single business operation across services.

---

# 43. Deployment Architecture

Initial local environment:

```text
Docker Compose
     │
     ├── API Gateway
     ├── Eureka
     ├── Auth Service
     ├── Employee Service
     ├── Skill Service
     ├── Project Service
     ├── Task Service
     ├── Workload Service
     ├── Recommendation Service
     ├── Performance Service
     ├── Notification Service
     ├── MySQL
     ├── Redis
     └── Kafka
```

---

# 44. AWS Deployment Architecture

Future deployment:

```text
                         Internet
                            │
                            ▼
                    AWS Load Balancer
                            │
                            ▼
                       API Gateway
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
          ECS/EC2       ECS/EC2       ECS/EC2
          Services      Services      Services
              │             │             │
              └──────┬──────┴──────┬──────┘
                     │             │
                     ▼             ▼
                   RDS           Redis
                  MySQL       ElastiCache
                     │
                     │
                     ▼
                   Kafka
                     │
                     ▼
                 Monitoring
                CloudWatch
```

The exact AWS deployment model may evolve.

---

# 45. CI/CD Architecture

```text
Developer
   ↓
Git Push
   ↓
GitHub
   ↓
GitHub Actions
   ↓
Build
   ↓
Unit Tests
   ↓
Integration Tests
   ↓
Quality Checks
   ↓
Docker Image
   ↓
Deployment
```

---

# 46. Architecture Principles

The following principles shall guide development.

### Principle 1 — Single Responsibility

Each service should have a clear business responsibility.

### Principle 2 — Loose Coupling

Services should minimize direct dependencies.

### Principle 3 — High Cohesion

Related business functionality should remain together.

### Principle 4 — API First

Service boundaries should be exposed through well-defined APIs.

### Principle 5 — Event Where Appropriate

Kafka should be used for asynchronous business events.

### Principle 6 — Database Ownership

Services should own their data.

### Principle 7 — Security by Default

Protected APIs require authentication and authorization.

### Principle 8 — Observable by Default

Important operations should be traceable through logs and correlation IDs.

### Principle 9 — Explainable Intelligence

Recommendation decisions should be explainable.

### Principle 10 — Human in the Loop

Managers retain final assignment authority.

---

# 47. Architecture Evolution

The architecture will evolve in stages.

## Version 1

```text
React
 ↓
API Gateway
 ↓
Spring Boot Services
 ↓
MySQL
 ↓
Redis
 ↓
Kafka
```

## Version 1.1

Add:

```text
Performance
Skill Gap
Learning Intelligence
Advanced Analytics
Notifications
```

## Version 2

Add:

```text
LLM
 ↓
RAG
 ↓
Vector Database
 ↓
Project Knowledge
 ↓
Jira/GitHub Integration
```

## Version 3

Potentially add:

```text
Predictive Workforce Planning
+
Skill Demand Forecasting
+
Automated Resource Optimization
```

---

# 48. Architecture Decision Summary

| Decision        | Choice                           | Reason                               |
| --------------- | -------------------------------- | ------------------------------------ |
| Backend         | Java + Spring Boot               | Enterprise ecosystem                 |
| API             | REST                             | Simple service communication         |
| Async Messaging | Kafka                            | Event-driven architecture            |
| Cache           | Redis                            | Fast frequently accessed data        |
| Database        | MySQL                            | Relational business data             |
| Gateway         | Spring Cloud Gateway             | Central API entry                    |
| Discovery       | Eureka                           | Dynamic service discovery            |
| Security        | Spring Security + JWT            | Authentication/RBAC                  |
| Frontend        | React + TypeScript               | Modern enterprise UI                 |
| Containers      | Docker                           | Reproducible deployment              |
| CI/CD           | GitHub Actions                   | Automated delivery                   |
| Cloud           | AWS                              | Enterprise cloud deployment          |
| Testing         | JUnit + Mockito + Testcontainers | Automated quality                    |
| AI              | LLM + deterministic rules        | Intelligent but controlled decisions |

---

# 49. Architecture Quality Goals

The architecture shall aim to achieve:

```text
Maintainability
       +
Scalability
       +
Reliability
       +
Security
       +
Observability
       +
Explainability
       +
Testability
       +
Cloud Readiness
```

The architecture is intentionally designed so that AI intelligence can evolve without requiring a complete rewrite of the core enterprise platform.

---

# 50. Final Architecture

The EWSO architecture can ultimately be summarized as:

```text
                         USERS
                           │
                           ▼
                  ┌────────────────┐
                  │ React Frontend │
                  └───────┬────────┘
                          │ HTTPS
                          ▼
                  ┌────────────────┐
                  │  API Gateway   │
                  └───────┬────────┘
                          │
       ┌──────────────────┼──────────────────┐
       │                  │                  │
       ▼                  ▼                  ▼
     Auth             Employee             Skill
    Service            Service             Service
       │                  │                  │
       └──────────────────┼──────────────────┘
                          │
             ┌────────────┴────────────┐
             │                         │
             ▼                         ▼
          Project                    Task
          Service                   Service
                                       │
                                       ▼
                              Recommendation
                                  Service
                                       │
                ┌──────────────────────┼──────────────────────┐
                │                      │                      │
                ▼                      ▼                      ▼
             Workload             Performance             AI Task
              Service               Service             Intelligence
                │                      │                      │
                └──────────────────────┼──────────────────────┘
                                       │
                                       ▼
                                    Kafka
                                       │
                   ┌───────────────────┼───────────────────┐
                   │                   │                   │
                   ▼                   ▼                   ▼
              Notification         Analytics             Audit
                Service              Service             Logging
                   
                   ┌───────────────────────────────────┐
                   │          Infrastructure            │
                   │                                   │
                   │ MySQL     Redis     Kafka          │
                   └───────────────────────────────────┘
                                       │
                                       ▼
                                      AWS
```

This architecture provides the foundation for implementing EWSO as a realistic enterprise-grade Java application rather than a simple CRUD project.
