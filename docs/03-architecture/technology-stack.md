# Technology Stack

## 1. Backend

| Technology      | Purpose                          |
| --------------- | -------------------------------- |
| Java 21         | Primary programming language     |
| Spring Boot     | Microservice development         |
| Spring Web      | REST APIs                        |
| Spring Data JPA | Database persistence             |
| Spring Security | Authentication and authorization |
| Spring Cloud    | Microservice infrastructure      |
| OpenFeign       | Service-to-service communication |
| Bean Validation | API input validation             |

## 2. Database

### MySQL

Used for persistent relational data including:

* Employees
* Skills
* Tasks
* Projects
* Workloads
* Performance
* Recommendations
* Users
* Audit logs

## 3. Messaging

### Apache Kafka

Used for asynchronous event-driven communication.

Example events:

* task.created
* task.assigned
* task.completed
* employee.updated
* employee.availability.changed
* recommendation.generated

## 4. Caching

### Redis

Used for frequently accessed and performance-sensitive data such as:

* Employee skill profiles
* Employee availability
* Workload information
* Recommendation results

## 5. Frontend

### React

Used to develop role-specific dashboards.

### TypeScript

Used for strongly typed frontend development.

## 6. AI

The AI layer will initially use deterministic scoring and structured task analysis.

Future versions may include:

* Large Language Models
* Embeddings
* Retrieval-Augmented Generation
* Vector databases

## 7. Testing

* JUnit 5
* Mockito
* Spring Boot Test
* Testcontainers

## 8. DevOps

* Git
* GitHub
* Docker
* GitHub Actions

## 9. Cloud

Amazon Web Services may be used for deployment.

Potential services include:

* EC2 / container hosting
* RDS
* ElastiCache
* Load Balancer
* CloudWatch
* IAM

The exact AWS architecture will be finalized during deployment design.
