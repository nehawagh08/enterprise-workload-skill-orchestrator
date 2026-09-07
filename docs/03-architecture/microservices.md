# Microservices Architecture

## 1. Overview

The **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)** follows a domain-oriented microservices architecture.

Each microservice owns a clearly defined business capability and is responsible for:

* Its own business logic
* Its own data ownership
* Its REST APIs
* Its Kafka events
* Its validation rules
* Its service-level security
* Its integration with other services

Services communicate using:

* **REST/OpenFeign** for synchronous operations
* **Apache Kafka** for asynchronous event-driven communication
* **Redis** for frequently accessed and temporary data

The architecture is designed so that individual services can be developed, tested, deployed, and scaled independently.

---

# 2. Microservices

The platform consists of the following services:

| Service                | Primary Responsibility                    | Phase |
| ---------------------- | ----------------------------------------- | ----- |
| API Gateway            | External entry point and routing          | MVP   |
| Service Registry       | Service discovery                         | MVP   |
| Auth Service           | Authentication and authorization          | MVP   |
| Employee Service       | Employee profiles and organizational data | MVP   |
| Skill Service          | Skills, proficiency and learning goals    | MVP   |
| Project Service        | Projects and project skill requirements   | MVP   |
| Task Service           | IT task/ticket lifecycle                  | MVP   |
| Workload Service       | Capacity and workload calculation         | MVP   |
| Recommendation Service | Intelligent employee-task matching        | MVP   |
| Performance Service    | Employee task performance                 | V1.1  |
| Notification Service   | Notifications and alerts                  | V1.1  |
| Analytics Service      | Workforce and organizational analytics    | V1.1  |
| Knowledge Service      | Enterprise technical knowledge/RAG        | V2    |
| AI Engine              | AI-powered task intelligence              | V2    |

---

# 3. API Gateway

## Responsibility

The API Gateway provides a single entry point for frontend and external clients.

Instead of allowing the React application to directly communicate with every microservice:

```text
React
  ↓
API Gateway
  ↓
Microservices
```

The gateway handles:

* Request routing
* JWT validation
* Authentication forwarding
* CORS
* Correlation ID generation
* Request logging
* Rate limiting
* Centralized API entry
* Basic authorization checks

## Example Routes

```text
/api/auth/**           → Auth Service
/api/employees/**      → Employee Service
/api/skills/**         → Skill Service
/api/projects/**       → Project Service
/api/tasks/**          → Task Service
/api/workloads/**      → Workload Service
/api/recommendations/**→ Recommendation Service
/api/performance/**    → Performance Service
/api/notifications/**  → Notification Service
/api/analytics/**      → Analytics Service
```

## Technology

* Spring Cloud Gateway
* Spring Security
* JWT
* Eureka Client

## Database

None.

The gateway should remain stateless.

---

# 4. Service Registry

## Responsibility

The Service Registry allows microservices to discover each other dynamically.

Example:

```text
Recommendation Service
        ↓
   Eureka Server
        ↓
Employee Service
```

The recommendation service does not need to hardcode:

```text
http://localhost:8082
```

Instead, it discovers the employee service through service registration.

## Technology

* Netflix Eureka Server
* Spring Cloud

## Database

None.

## APIs

The service primarily provides service-discovery infrastructure rather than business APIs.

---

# 5. Auth Service

## Responsibility

The Auth Service manages:

* User authentication
* Login
* Password hashing
* JWT generation
* Role management
* Token validation support
* Account status
* Authentication-related security

## Main Entities

```text
users
roles
user_roles
```

## Example API

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "username": "neha",
  "password": "password"
}
```

Response:

```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "role": "EMPLOYEE"
}
```

## Roles

```text
ADMIN
MANAGER
TEAM_LEAD
PROJECT_MANAGER
EMPLOYEE
```

## Dependencies

* MySQL
* Spring Security
* JWT
* Employee Service

## Events

```text
user.created
user.status.changed
```

---

# 6. Employee Service

## Responsibility

Employee Service owns employee organizational information.

It manages:

* Employee profile
* Department
* Designation
* Experience
* Manager relationship
* Availability
* Employment status
* Contact information required by the platform

## Main Entities

```text
employees
departments
employee_availability
```

## Example APIs

```http
POST   /api/employees
GET    /api/employees/{id}
GET    /api/employees
PUT    /api/employees/{id}
PATCH  /api/employees/{id}/availability
GET    /api/employees/{id}/manager
GET    /api/departments/{id}/employees
```

## Example Employee

```json
{
  "employeeId": 101,
  "employeeCode": "EMP-101",
  "name": "Employee 101",
  "department": "Engineering",
  "designation": "Java Developer",
  "yearsOfExperience": 3.2,
  "employmentStatus": "ACTIVE",
  "availabilityStatus": "AVAILABLE"
}
```

## Database Ownership

```text
employees
departments
employee_availability
```

The Employee Service is the **only service allowed to directly modify these tables**.

Other services access employee information through APIs or events.

## Events Published

```text
employee.created
employee.updated
employee.availability.changed
employee.deactivated
```

---

# 7. Skill Service

## Responsibility

Skill Service manages the technical skill ecosystem.

It handles:

* Skill catalogue
* Employee skills
* Proficiency
* Years of experience
* Skill verification
* Learning goals
* Desired future skills

## Proficiency Levels

```text
1 → Beginner
2 → Basic
3 → Intermediate
4 → Advanced
5 → Expert
```

## Main Entities

```text
skills
employee_skills
learning_goals
```

## Example APIs

```http
GET    /api/skills
POST   /api/skills
GET    /api/employees/{id}/skills
POST   /api/employees/{id}/skills
PUT    /api/employees/{id}/skills/{skillId}
DELETE /api/employees/{id}/skills/{skillId}

GET    /api/employees/{id}/learning-goals
POST   /api/employees/{id}/learning-goals
```

## Example Skill Profile

```json
{
  "employeeId": 101,
  "skills": [
    {
      "skill": "Java",
      "proficiency": 5,
      "yearsOfExperience": 3.5
    },
    {
      "skill": "Spring Boot",
      "proficiency": 4,
      "yearsOfExperience": 2.8
    },
    {
      "skill": "AWS",
      "proficiency": 3,
      "yearsOfExperience": 1.0
    }
  ]
}
```

## Events

```text
skill.created
employee.skill.updated
employee.learning-goal.updated
```

---

# 8. Project Service

## Responsibility

Project Service manages project-level information.

It handles:

* Project creation
* Project metadata
* Project managers
* Team relationships
* Project status
* Required project skills
* Project capacity requirements

## Main Entities

```text
projects
project_skills
project_members
```

## Example APIs

```http
POST /api/projects
GET  /api/projects/{id}
GET  /api/projects
PUT  /api/projects/{id}

POST /api/projects/{id}/skills
GET  /api/projects/{id}/skills

POST /api/projects/{id}/members
GET  /api/projects/{id}/members
```

## Example

```json
{
  "projectId": 501,
  "projectName": "Enterprise Payment Platform",
  "status": "ACTIVE",
  "requiredSkills": [
    "Java",
    "Spring Boot",
    "Microservices",
    "AWS"
  ]
}
```

## Events

```text
project.created
project.updated
project.skill-requirement.changed
project.member.added
```

---

# 9. Task Service

## Responsibility

Task Service owns the complete IT task/ticket lifecycle.

It manages:

* Task creation
* Task description
* Priority
* Complexity
* Deadline
* Estimated effort
* Required skills
* Project association
* Assignment
* Status
* Reassignment

## Main Entities

```text
tasks
task_skills
task_assignments
```

## Task Status

```text
OPEN
IN_PROGRESS
BLOCKED
COMPLETED
CANCELLED
```

## Priority

```text
LOW
MEDIUM
HIGH
CRITICAL
```

## Complexity

```text
SIMPLE
MEDIUM
COMPLEX
EXPERT
```

## Example API

```http
POST  /api/tasks
GET   /api/tasks/{id}
GET   /api/tasks
PUT   /api/tasks/{id}
PATCH /api/tasks/{id}/status
POST  /api/tasks/{id}/assign
POST  /api/tasks/{id}/reassign
```

## Example Task

```json
{
  "taskId": "TASK-1024",
  "title": "Fix payment service timeout",
  "description": "Payment API is timing out during provider communication",
  "priority": "HIGH",
  "complexity": "COMPLEX",
  "estimatedHours": 6,
  "deadline": "2026-09-10T18:00:00",
  "requiredSkills": [
    "Java",
    "Spring Boot",
    "Microservices",
    "Payment Systems"
  ]
}
```

## Events

```text
task.created
task.updated
task.assigned
task.reassigned
task.completed
task.blocked
task.cancelled
```

Task Service does **not** calculate employee suitability.

That responsibility belongs to Recommendation Service.

---

# 10. Workload Service

## Responsibility

Workload Service calculates employee capacity and workload.

It answers questions such as:

* How busy is an employee?
* How many hours are already assigned?
* How much capacity remains?
* Is the employee overloaded?
* How much capacity does a team have?

## Workload Formula

```text
Workload %
=
Assigned Hours / Available Capacity Hours × 100
```

Example:

```text
Available capacity = 40 hours
Assigned work = 24 hours

Workload = 24 / 40 × 100
         = 60%
```

## Workload States

```text
0–40%    → AVAILABLE
41–70%   → NORMAL
71–85%   → BUSY
86–100%  → OVERLOADED
>100%    → OVER_CAPACITY
```

Thresholds should be configurable.

## APIs

```http
GET /api/workloads/employees/{id}
GET /api/workloads/teams/{id}
GET /api/workloads/overloaded
GET /api/workloads/available
```

## Events Consumed

```text
task.assigned
task.reassigned
task.completed
employee.availability.changed
```

## Events Published

```text
workload.updated
employee.overloaded
employee.capacity.available
```

## Redis

Frequently accessed workload data can be cached:

```text
employee:{employeeId}:workload
team:{teamId}:workload
```

---

# 11. Recommendation Service

## Responsibility

The Recommendation Service is the **core intelligence service** of EWSO.

Its job is to determine:

> "Which employees are the most suitable candidates for this task?"

It evaluates:

* Technical skills
* Skill proficiency
* Relevant experience
* Current workload
* Availability
* Task complexity
* Past performance
* Learning opportunity
* Deadline risk

## Recommendation Flow

```text
Task
 ↓
Required Skills
 ↓
Find Eligible Employees
 ↓
Fetch Employee Skills
 ↓
Fetch Workload
 ↓
Fetch Experience
 ↓
Fetch Performance
 ↓
Evaluate Learning Opportunity
 ↓
Calculate Score
 ↓
Rank Candidates
 ↓
Generate Explanation
 ↓
Return Recommendations
```

## Recommendation Score

Initial weighting:

```text
Skill Match          30%
Past Experience      20%
Current Workload     20%
Task Complexity      10%
Availability         10%
Past Performance      5%
Learning Opportunity  5%
--------------------------------
Total               100%
```

## Example

```json
{
  "taskId": "TASK-1024",
  "recommendations": [
    {
      "employeeId": 101,
      "score": 91.2,
      "confidence": 0.91,
      "rank": 1,
      "reason": "Strong Spring Boot and payment-domain experience"
    },
    {
      "employeeId": 107,
      "score": 84.7,
      "confidence": 0.87,
      "rank": 2,
      "reason": "Strong Java and microservices skills with moderate workload"
    }
  ]
}
```

## APIs

```http
POST /api/recommendations/tasks/{taskId}
GET  /api/recommendations/tasks/{taskId}
GET  /api/recommendations/{recommendationId}
POST /api/recommendations/{id}/accept
POST /api/recommendations/{id}/reject
POST /api/recommendations/{id}/override
```

## Service Dependencies

```text
Recommendation Service
        |
        +---- Employee Service
        |
        +---- Skill Service
        |
        +---- Workload Service
        |
        +---- Performance Service
        |
        +---- Task Service
```

Communication should primarily use REST/OpenFeign for data required during recommendation generation.

Redis may be used for:

```text
employee skill profiles
employee availability
workload
recent recommendations
```

## Recommendation Events

```text
recommendation.generated
recommendation.accepted
recommendation.rejected
recommendation.overridden
```

---

# 12. Recommendation Internal Architecture

The Recommendation Service should not contain all logic inside a controller.

Recommended structure:

```text
recommendation-service
│
├── controller
│   └── RecommendationController
│
├── service
│   ├── RecommendationService
│   ├── CandidateService
│   ├── ScoringService
│   ├── ExplanationService
│   └── ConfidenceService
│
├── strategy
│   ├── SkillMatchStrategy
│   ├── WorkloadStrategy
│   ├── ExperienceStrategy
│   ├── AvailabilityStrategy
│   ├── PerformanceStrategy
│   └── LearningOpportunityStrategy
│
├── client
│   ├── EmployeeClient
│   ├── SkillClient
│   ├── WorkloadClient
│   ├── TaskClient
│   └── PerformanceClient
│
├── model
├── repository
├── mapper
├── event
├── exception
└── config
```

This keeps the recommendation engine maintainable and testable.

---

# 13. Performance Service

## Responsibility

Performance Service maintains historical employee performance information.

It can track:

* Tasks completed
* Average completion time
* On-time completion
* Reopened tasks
* Incident resolution
* Quality score
* Manager feedback

## Main Entities

```text
performance_metrics
task_performance
manager_feedback
```

## Example API

```http
GET  /api/performance/employees/{id}
POST /api/performance/tasks/{taskId}
GET  /api/performance/teams/{teamId}
```

## Events Consumed

```text
task.completed
task.reopened
incident.resolved
```

## Events Published

```text
employee.performance.updated
```

Performance information is consumed by Recommendation Service.

---

# 14. Notification Service

## Responsibility

Notification Service handles asynchronous notifications.

Examples:

* New task assigned
* Task deadline approaching
* Task overdue
* Task reassigned
* Recommendation accepted
* Recommendation rejected
* Skill gap identified
* Employee overloaded

## Notification Channels

Initial implementation:

```text
In-app notification
```

Future:

```text
Email
Teams
Slack
Mobile push
```

## Main Entity

```text
notifications
```

## Kafka Events Consumed

```text
task.assigned
task.reassigned
task.deadline.approaching
employee.overloaded
skill-gap.detected
recommendation.accepted
```

## API

```http
GET  /api/notifications
PATCH /api/notifications/{id}/read
PATCH /api/notifications/read-all
```

---

# 15. Analytics Service

## Responsibility

Analytics Service provides management-level insights.

Examples:

### Employee Analytics

```text
Employee workload
Task completion rate
Skill distribution
Performance trend
```

### Team Analytics

```text
Team utilization
Open tasks
Overloaded employees
Available capacity
Skill coverage
```

### Organization Analytics

```text
Most demanded skills
Skill gaps
Resource utilization
Task distribution
Project capacity
```

## Example APIs

```http
GET /api/analytics/workforce
GET /api/analytics/teams/{id}
GET /api/analytics/skills
GET /api/analytics/workload
GET /api/analytics/performance
```

Analytics should avoid directly modifying data owned by other services.

For larger deployments, it can consume Kafka events and maintain read-optimized analytical models.

---

# 16. AI Engine

## Responsibility

The AI Engine enhances the platform with intelligent task understanding.

The initial recommendation engine should remain deterministic.

AI should enhance—not replace—the business rules.

## AI Capabilities

### Task Classification

Example:

```text
Input:
"Payment API fails intermittently while calling PayPal provider."

Output:

Domain:
Payment Systems

Skills:
Java
Spring Boot
REST APIs
Microservices
Payment Integration

Complexity:
COMPLEX
```

### Skill Extraction

The AI identifies technical requirements from unstructured task descriptions.

### Task Summarization

Converts long incident descriptions into concise summaries.

### Similar Task Detection

Finds previously solved tasks with similar characteristics.

### Risk Identification

Detects possible deadline or complexity risks.

---

# 17. Knowledge Service — Future

The Knowledge Service is planned for the advanced version of EWSO.

It will ingest technical project information such as:

```text
Jira tickets
Git commits
Pull requests
API documentation
Architecture documents
Incident reports
Database changes
Runbooks
```

The information can later be stored in a searchable/vector-based knowledge layer.

Example questions:

```text
Why was Circuit Breaker added to Payment Service?

Which APIs are affected if transaction_status changes?

Which developer previously solved a similar PayPal timeout?

What caused the previous payment-service incident?
```

This becomes the platform's **Enterprise Project Knowledge Memory**.

---

# 18. Service-to-Service Communication

## Synchronous Communication

Use REST/OpenFeign when the calling service requires an immediate response.

Example:

```text
Recommendation Service
        |
        | REST
        ↓
Skill Service
        |
        ↓
Employee Skill Profile
```

Appropriate for:

* Candidate evaluation
* Employee lookup
* Skill lookup
* Workload lookup
* Task details

---

# 19. Asynchronous Communication

Kafka is used when services do not need an immediate response.

Example:

```text
Task Service
     |
     | task.assigned
     ↓
   Kafka
     |
     +------→ Workload Service
     |
     +------→ Notification Service
     |
     +------→ Performance Service
     |
     +------→ Analytics Service
```

This prevents tight coupling between services.

---

# 20. Kafka Event Ownership

| Event                           | Producer       | Consumers                         |
| ------------------------------- | -------------- | --------------------------------- |
| `task.created`                  | Task           | Recommendation, Analytics         |
| `task.assigned`                 | Task           | Workload, Notification, Analytics |
| `task.completed`                | Task           | Workload, Performance, Analytics  |
| `task.blocked`                  | Task           | Notification, Analytics           |
| `employee.created`              | Employee       | Auth, Analytics                   |
| `employee.availability.changed` | Employee       | Workload, Recommendation          |
| `employee.skill.updated`        | Skill          | Recommendation, Analytics         |
| `employee.performance.updated`  | Performance    | Recommendation, Analytics         |
| `recommendation.generated`      | Recommendation | Analytics, Notification           |
| `recommendation.accepted`       | Recommendation | Task, Analytics                   |
| `employee.overloaded`           | Workload       | Notification, Analytics           |

---

# 21. Database Ownership

Each service logically owns its own data.

```text
Auth Service
    → users, roles

Employee Service
    → employees, departments, availability

Skill Service
    → skills, employee_skills, learning_goals

Project Service
    → projects, project_skills, project_members

Task Service
    → tasks, task_skills, task_assignments

Workload Service
    → workloads, capacity

Performance Service
    → performance_metrics, task_performance

Recommendation Service
    → recommendations, recommendation_factors

Notification Service
    → notifications
```

### Important Rule

No service should directly modify another service's database.

For example:

```text
❌ Recommendation Service
       ↓
   UPDATE employee_skills
```

Instead:

```text
Recommendation Service
       ↓
   Skill Service API
```

or consume:

```text
employee.skill.updated
```

---

# 22. Initial Database Strategy

For portfolio development, all services can initially run using one MySQL deployment.

Example:

```text
MySQL
│
├── ewso_auth
├── ewso_employee
├── ewso_skill
├── ewso_project
├── ewso_task
├── ewso_workload
├── ewso_recommendation
├── ewso_performance
└── ewso_notification
```

This provides logical database ownership while keeping local development manageable.

A production deployment can later move services to independently managed databases.

---

# 23. Redis Architecture

Redis is used for high-frequency read operations.

Example keys:

```text
employee:{id}:skills
employee:{id}:availability
employee:{id}:workload
team:{id}:workload
recommendation:{taskId}
skills:catalogue
```

Example:

```text
GET recommendation:TASK-1024
```

If the recommendation is available in cache:

```text
Redis
 ↓
Return result
```

Otherwise:

```text
Recommendation Service
 ↓
Calculate recommendation
 ↓
Store in Redis
 ↓
Return result
```

Cache invalidation should occur when relevant employee, skill, workload, or task data changes.

---

# 24. Failure Handling Between Services

Every service-to-service call should use appropriate resilience mechanisms.

Recommended:

```text
Timeout
   ↓
Retry
   ↓
Circuit Breaker
   ↓
Fallback
```

Example:

```text
Recommendation Service
       ↓
Workload Service
       ↓
   Timeout
       ↓
Circuit Breaker
       ↓
Fallback
```

The system should not fail completely because a non-critical supporting service is temporarily unavailable.

---

# 25. Recommendation Failure Strategy

The Recommendation Service should have graceful degradation.

Example:

If:

```text
Performance Service unavailable
```

the recommendation engine may continue using:

```text
Skills
Experience
Workload
Availability
Task complexity
```

and treat missing performance data neutrally.

It should **not** automatically interpret:

```text
missing performance data = poor performance
```

Similarly, AI failure must not prevent managers from manually assigning tasks.

---

# 26. Human-in-the-Loop Architecture

The platform recommends.

The manager decides.

```text
Task
 ↓
Recommendation Engine
 ↓
Top Candidates
 ↓
Manager Review
 ↓
┌───────────────┬────────────────┐
│ Accept        │ Override       │
↓               ↓
Assignment      Manual Candidate
```

Manager overrides should be recorded for audit and future analysis.

Example:

```json
{
  "recommendationId": 9001,
  "recommendedEmployee": 101,
  "selectedEmployee": 108,
  "overrideReason": "Employee 108 has domain knowledge not captured in the system"
}
```

This is important because enterprise AI systems should support human decision-making rather than silently making high-impact staffing decisions.

---

# 27. Microservice Internal Structure

Every Spring Boot service should follow a consistent structure.

Example:

```text
employee-service/
│
├── src/main/java/com/ewso/employee/
│
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── mapper/
│   ├── exception/
│   ├── validation/
│   ├── event/
│   ├── config/
│   └── EmployeeServiceApplication.java
│
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│
├── src/test/
│
├── Dockerfile
└── pom.xml
```

Recommended layers:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

External communication:

```text
Controller
    ↓
Service
    ↓
Feign Client / Kafka Producer
```

---

# 28. Service Dependency Overview

```text
                         ┌──────────────────┐
                         │   API Gateway    │
                         └────────┬─────────┘
                                  │
             ┌────────────────────┼────────────────────┐
             ↓                    ↓                    ↓
       Auth Service        Employee Service       Task Service
                                  │                    │
                                  ↓                    ↓
                           Skill Service       Project Service
                                  │                    │
                                  └─────────┬──────────┘
                                            ↓
                                  Recommendation Service
                                     ↙      ↓       ↘
                                    ↓       ↓        ↓
                              Workload  Performance  Redis
                               Service    Service
                                   
                                  Kafka
                                    │
                  ┌─────────────────┼─────────────────┐
                  ↓                 ↓                 ↓
             Notification       Analytics        Future AI/
                Service           Service        Knowledge
```

---

# 29. MVP Service Boundary

The first implementation should **not attempt to build every service simultaneously**.

### MVP

```text
API Gateway
Service Registry
Auth Service
Employee Service
Skill Service
Project Service
Task Service
Workload Service
Recommendation Service
```

Supporting infrastructure:

```text
MySQL
Redis
Kafka
Docker
```

### V1.1

Add:

```text
Performance Service
Notification Service
Analytics Service
Learning Goals
Skill Gap Analysis
Growth-Aware Allocation
```

### V2

Add:

```text
AI Engine
Knowledge Service
RAG
Vector Database
Jira Integration
GitHub Integration
Incident Intelligence
Predictive Workforce Planning
```

This staged approach keeps the project realistic and prevents the portfolio implementation from becoming an unfinished collection of services.

---

# 30. Recommended Implementation Order

The recommended development sequence is:

```text
1. Service Registry
        ↓
2. API Gateway
        ↓
3. Auth Service
        ↓
4. Employee Service
        ↓
5. Skill Service
        ↓
6. Project Service
        ↓
7. Task Service
        ↓
8. Workload Service
        ↓
9. Recommendation Service
        ↓
10. Redis
        ↓
11. Kafka
        ↓
12. Performance Service
        ↓
13. Notification Service
        ↓
14. Analytics Service
        ↓
15. React Frontend
        ↓
16. AI Task Intelligence
        ↓
17. Skill Gap + Growth Engine
        ↓
18. Knowledge/RAG Layer
        ↓
19. AWS Deployment
```

---

# 31. Core Architectural Principle

The most important architectural rule of EWSO is:

> **Business ownership stays inside the service that owns the domain.**

For example:

```text
Employee data
→ Employee Service

Skill data
→ Skill Service

Task lifecycle
→ Task Service

Workload calculation
→ Workload Service

Recommendation decision
→ Recommendation Service

Performance metrics
→ Performance Service
```

This prevents the project from becoming a distributed monolith.

---

# 32. Final Architecture Goal

The completed platform should demonstrate real enterprise engineering practices:

```text
                    React
                      │
                      ↓
                API Gateway
                      │
              ┌───────┴───────┐
              │               │
         Authentication    Routing
              │               │
              └───────┬───────┘
                      ↓
               Microservices
                      │
        ┌─────────────┼─────────────┐
        ↓             ↓             ↓
      REST          Kafka         Redis
        │             │             │
        └─────────────┼─────────────┘
                      ↓
                    MySQL
                      │
                      ↓
                 AWS / Cloud
```

The final system should demonstrate not only Java and Spring Boot development, but also:

* Microservice design
* REST API development
* Spring Security
* JWT
* Database design
* OpenFeign
* Kafka
* Redis
* Event-driven architecture
* Resilience patterns
* Testing
* Docker
* CI/CD
* AWS
* AI integration
* Enterprise authorization
* Observability
* Auditability
* Human-in-the-loop AI
* Explainable recommendations
* Scalable architecture
