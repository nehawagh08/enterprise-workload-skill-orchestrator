# Docker Deployment & Containerization

## 1. Purpose

This document defines the Docker-based containerization strategy for the **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)**.

Docker provides a consistent environment for developing, testing, and running the EWSO microservices and supporting infrastructure.

The Docker architecture supports:

* Local development
* Microservice isolation
* Reproducible environments
* Service-to-service networking
* MySQL persistence
* Redis caching
* Apache Kafka messaging
* Health checks
* Environment-based configuration
* CI/CD integration
* Future AWS deployment

---

# 2. Containerization Strategy

Each independently deployable backend service is packaged as a separate Docker image.

```text
                         EWSO Docker Environment
                                  |
              +-------------------+-------------------+
              |                                       |
        Application Layer                    Infrastructure Layer
              |                                       |
     +--------+--------+                    +---------+---------+
     |        |        |                    |         |         |
  Gateway   Auth    Employee              MySQL     Redis     Kafka
     |        |        |
     +--------+--------+
              |
       Other Services
              |
    +---------+---------+---------+---------+
    |         |         |         |         |
  Skill    Project     Task    Workload  Recommendation
    |
 Performance / Notification / Analytics
```

The frontend is also containerized separately.

---

# 3. Docker Components

The local environment contains the following major containers.

| Container                | Purpose                                         |
| ------------------------ | ----------------------------------------------- |
| `api-gateway`            | External API entry point                        |
| `service-registry`       | Eureka service discovery                        |
| `auth-service`           | Authentication and authorization                |
| `employee-service`       | Employee profiles                               |
| `skill-service`          | Skills and learning goals                       |
| `project-service`        | Project management                              |
| `task-service`           | Task management                                 |
| `workload-service`       | Workload calculation                            |
| `recommendation-service` | Employee-task recommendation                    |
| `performance-service`    | Performance tracking                            |
| `notification-service`   | Notifications                                   |
| `analytics-service`      | Workforce analytics                             |
| `knowledge-service`      | Future knowledge/RAG functionality              |
| `web-app`                | React frontend                                  |
| `mysql`                  | Relational database                             |
| `redis`                  | Cache                                           |
| `kafka`                  | Event streaming                                 |
| `zookeeper`              | Kafka dependency if using ZooKeeper-based Kafka |

---

# 4. Repository Docker Structure

Recommended repository structure:

```text
enterprise-workload-skill-orchestrator/
│
├── backend/
│   ├── api-gateway/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── service-registry/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── auth-service/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── employee-service/
│   ├── skill-service/
│   ├── project-service/
│   ├── task-service/
│   ├── workload-service/
│   ├── recommendation-service/
│   ├── performance-service/
│   ├── notification-service/
│   ├── analytics-service/
│   └── knowledge-service/
│
├── frontend/
│   └── web-app/
│       └── Dockerfile
│
├── infrastructure/
│   ├── docker/
│   │   ├── mysql/
│   │   ├── redis/
│   │   └── kafka/
│   │
│   └── ...
│
├── docker-compose.yml
├── .env.example
└── .dockerignore
```

---

# 5. Dockerfile Strategy

Every Spring Boot service should have its own Dockerfile.

The preferred approach is a **multi-stage build**.

This separates:

1. Application compilation
2. Runtime execution

Benefits:

* Smaller final image
* Reduced attack surface
* Build dependencies excluded from runtime
* Better production practice

---

# 6. Backend Dockerfile

Example Dockerfile for a Spring Boot service:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

The same pattern can be used for:

* employee-service
* skill-service
* project-service
* task-service
* workload-service
* recommendation-service
* performance-service
* notification-service
* analytics-service

Port numbers should be configured according to the service architecture.

---

# 7. Improved Production Dockerfile

For production, the runtime image should run with a non-root user.

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home appuser

COPY --from=builder /build/target/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

This prevents the application from running as root inside the container.

---

# 8. Frontend Dockerfile

The React application can use a multi-stage build.

```dockerfile
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build


FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

The frontend container is responsible only for serving the compiled React application.

---

# 9. .dockerignore

Every application should have a `.dockerignore`.

Example:

```text
target/
node_modules/
.git/
.github/
.idea/
.vscode/
*.log
.env
.DS_Store
README.md
```

The `.env` file must not be copied into Docker images.

Secrets must never be baked into an image.

---

# 10. Docker Compose

Docker Compose provides the complete local EWSO environment.

The root-level file is:

```text
docker-compose.yml
```

The Compose environment should contain:

```text
Frontend
   |
API Gateway
   |
+-----------------------------+
|      Spring Services        |
+-----------------------------+
   |
+----------+---------+---------+
|          |         |
MySQL     Redis     Kafka
```

---

# 11. Docker Network

All EWSO containers should communicate through a dedicated Docker network.

Example:

```yaml
networks:
  ewso-network:
    driver: bridge
```

Services can then communicate using container/service names.

For example:

```text
http://employee-service:8081
http://skill-service:8082
http://task-service:8083
http://redis:6379
```

The application should **not** use:

```text
localhost
```

for container-to-container communication.

Inside a container, `localhost` refers to that same container.

---

# 12. MySQL Container

Example:

```yaml
mysql:
  image: mysql:8.4
  container_name: ewso-mysql
  restart: unless-stopped

  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    MYSQL_DATABASE: ewso

  ports:
    - "3306:3306"

  volumes:
    - mysql_data:/var/lib/mysql

  networks:
    - ewso-network

  healthcheck:
    test:
      [
        "CMD",
        "mysqladmin",
        "ping",
        "-h",
        "localhost"
      ]
    interval: 10s
    timeout: 5s
    retries: 5
```

---

# 13. MySQL Database Strategy

For local development, a single MySQL container can host multiple schemas.

Example:

```text
ewso_auth
ewso_employee
ewso_skill
ewso_project
ewso_task
ewso_workload
ewso_recommendation
ewso_performance
ewso_notification
ewso_analytics
```

Each microservice owns its own schema.

For example:

```text
employee-service
        |
        v
    ewso_employee
```

```text
task-service
        |
        v
      ewso_task
```

Services must not directly modify another service's schema.

---

# 14. Redis Container

Redis is used for frequently accessed data.

Example:

```yaml
redis:
  image: redis:7-alpine
  container_name: ewso-redis
  restart: unless-stopped

  command:
    - redis-server
    - --appendonly
    - "yes"

  ports:
    - "6379:6379"

  volumes:
    - redis_data:/data

  networks:
    - ewso-network

  healthcheck:
    test:
      [
        "CMD",
        "redis-cli",
        "ping"
      ]
    interval: 10s
    timeout: 5s
    retries: 5
```

Redis can cache:

```text
employee:{id}:skills
employee:{id}:availability
employee:{id}:workload
employee:{id}:performance
recommendation:{taskId}
skills:catalogue
```

---

# 15. Kafka Container

Kafka is used for asynchronous communication.

Example topics:

```text
task-events
employee-events
skill-events
recommendation-events
performance-events
notification-events
```

Business events include:

```text
task.created
task.assigned
task.completed
task.blocked

employee.availability.changed
employee.skill.updated

recommendation.generated
recommendation.accepted
recommendation.rejected
recommendation.overridden

employee.performance.updated
```

Kafka configuration should use a Docker-compatible listener configuration.

For a local setup, the Kafka container should expose a listener accessible by other EWSO containers.

---

# 16. Kafka Persistence

Kafka data should use a Docker volume:

```yaml
volumes:
  kafka_data:
```

This prevents event data from being lost when the container is recreated during local development.

For production, Kafka persistence and replication must be designed separately from the local Docker Compose setup.

---

# 17. Environment Variables

Application configuration must be externalized.

Example `.env.example`:

```env
MYSQL_ROOT_PASSWORD=change-me
MYSQL_DATABASE=ewso

MYSQL_HOST=mysql
MYSQL_PORT=3306

REDIS_HOST=redis
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=kafka:9092

JWT_SECRET=change-me

SPRING_PROFILES_ACTIVE=docker
```

The actual `.env` file must not be committed.

Add:

```text
.env
```

to `.gitignore`.

---

# 18. Spring Boot Configuration

Example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
```

This allows the same application image to run in:

```text
Local
Docker
Testing
AWS
```

without changing the image.

---

# 19. Service Configuration

Each microservice should receive its configuration through environment variables.

Example:

```yaml
employee-service:
  environment:
    SPRING_PROFILES_ACTIVE: docker

    DB_HOST: mysql
    DB_PORT: 3306
    DB_NAME: ewso_employee

    DB_USERNAME: ${DB_USERNAME}
    DB_PASSWORD: ${DB_PASSWORD}

    REDIS_HOST: redis
    REDIS_PORT: 6379

    KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

---

# 20. Service Startup Dependencies

Docker Compose `depends_on` should be used carefully.

Example:

```yaml
depends_on:
  mysql:
    condition: service_healthy
  redis:
    condition: service_healthy
```

However, `depends_on` does not guarantee that the entire application is ready.

Spring Boot services should therefore also implement:

* connection retry
* timeout
* graceful startup
* health checks

---

# 21. Health Checks

Spring Boot Actuator should expose:

```text
/actuator/health
```

Example:

```yaml
healthcheck:
  test:
    [
      "CMD",
      "curl",
      "-f",
      "http://localhost:8080/actuator/health"
    ]
  interval: 30s
  timeout: 10s
  retries: 5
```

Health checks allow Docker and future orchestration platforms to determine whether a service is operational.

---

# 22. API Gateway Container

The API Gateway is the primary external entry point.

Example:

```text
Browser
   |
   v
web-app
   |
   v
api-gateway:8080
   |
   +--> auth-service
   +--> employee-service
   +--> skill-service
   +--> project-service
   +--> task-service
   +--> workload-service
   +--> recommendation-service
```

Individual backend services should not need to be exposed publicly.

---

# 23. Internal vs External Ports

For local development, ports can be exposed for debugging.

Example:

```text
Gateway                 8080
Auth                    8081
Employee                8082
Skill                   8083
Project                 8084
Task                    8085
Workload                8086
Recommendation          8087
Performance             8088
Notification            8089
Analytics               8090
Frontend                3000
MySQL                   3306
Redis                   6379
Kafka                   9092
```

In a production deployment, backend services should generally remain private behind the gateway/load balancer.

---

# 24. Example Docker Compose Service

```yaml
employee-service:
  build:
    context: ./backend/employee-service
    dockerfile: Dockerfile

  container_name: ewso-employee-service

  environment:
    SPRING_PROFILES_ACTIVE: docker

    DB_HOST: mysql
    DB_PORT: 3306
    DB_NAME: ewso_employee
    DB_USERNAME: ${DB_USERNAME}
    DB_PASSWORD: ${DB_PASSWORD}

    REDIS_HOST: redis
    REDIS_PORT: 6379

    KAFKA_BOOTSTRAP_SERVERS: kafka:9092

  ports:
    - "8082:8080"

  depends_on:
    mysql:
      condition: service_healthy
    redis:
      condition: service_healthy

  networks:
    - ewso-network
```

---

# 25. Complete Local Architecture

The local Docker environment should conceptually look like:

```text
                         Browser
                            |
                            v
                    +---------------+
                    |   web-app     |
                    |    React      |
                    +-------+-------+
                            |
                            v
                    +---------------+
                    |  API Gateway  |
                    |    :8080      |
                    +-------+-------+
                            |
        +-------------------+--------------------+
        |         |         |          |         |
        v         v         v          v         v
      Auth     Employee    Task      Workload   Skill
        |         |         |          |         |
        +---------+---------+----------+---------+
                            |
                     Recommendation
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
            MySQL         Redis         Kafka
```

---

# 26. Docker Volumes

Persistent data should use named volumes.

```yaml
volumes:
  mysql_data:
  redis_data:
  kafka_data:
```

Benefits:

* Data survives container restart
* Database state persists
* Redis persistence can be maintained
* Kafka local data persists

---

# 27. Docker Compose Commands

Start the complete environment:

```bash
docker compose up -d
```

Build and start:

```bash
docker compose up -d --build
```

View running containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs
```

View logs for a specific service:

```bash
docker compose logs -f recommendation-service
```

Stop services:

```bash
docker compose down
```

Stop and remove volumes:

```bash
docker compose down -v
```

Rebuild one service:

```bash
docker compose build recommendation-service
```

Restart one service:

```bash
docker compose restart recommendation-service
```

---

# 28. Development Workflow

Recommended workflow:

```text
Developer changes code
        |
        v
Run unit tests
        |
        v
Build Spring Boot application
        |
        v
Build Docker image
        |
        v
docker compose up
        |
        v
Integration testing
        |
        v
API testing
        |
        v
Commit code
        |
        v
Push to GitHub
```

---

# 29. Docker Image Naming

Recommended naming convention:

```text
ewso/api-gateway
ewso/auth-service
ewso/employee-service
ewso/skill-service
ewso/project-service
ewso/task-service
ewso/workload-service
ewso/recommendation-service
ewso/performance-service
ewso/notification-service
ewso/analytics-service
ewso/web-app
```

Versioned images should use tags:

```text
ewso/recommendation-service:1.0.0
ewso/recommendation-service:1.1.0
```

Avoid relying only on:

```text
latest
```

for production deployments.

---

# 30. Docker Image Security

Docker images should follow these rules:

### Rule 1 — Minimal base image

Prefer:

```text
eclipse-temurin:21-jre
```

instead of a full development image for runtime.

### Rule 2 — Multi-stage builds

Build tools should not remain in the runtime image.

### Rule 3 — Non-root execution

Production containers should run as an unprivileged user.

### Rule 4 — No secrets

Never include:

```text
JWT secrets
database passwords
API keys
AWS credentials
LLM API keys
```

inside Dockerfiles.

### Rule 5 — Scan images

CI/CD should scan images for known vulnerabilities.

### Rule 6 — Pin important dependencies

Use controlled versions for base images and dependencies.

---

# 31. Secrets Management

Local development:

```text
.env
```

or Docker secrets where appropriate.

Production:

```text
AWS Secrets Manager
```

should be preferred.

Examples of secrets:

```text
DB_PASSWORD
JWT_SECRET
PAYPAL_CLIENT_SECRET
LLM_API_KEY
KAFKA_CREDENTIALS
```

Secrets should never be committed to GitHub.

---

# 32. Docker Logging

Containers should write application logs to standard output.

Example:

```text
docker compose logs -f task-service
```

The application should avoid depending on container-local log files.

Production environments can forward container logs to centralized monitoring such as:

```text
AWS CloudWatch
```

---

# 33. Correlation ID

The API Gateway generates a correlation ID when the request does not already contain one.

Example:

```text
X-Correlation-ID: REQ-82A71
```

The ID should propagate through:

```text
Gateway
   ↓
Task Service
   ↓
Recommendation Service
   ↓
Employee Service
   ↓
Kafka
```

This makes distributed debugging significantly easier.

---

# 34. Container Resource Management

Production containers should have resource limits.

Conceptually:

```text
CPU limit
Memory limit
CPU reservation
Memory reservation
```

This prevents one service from consuming all available host resources.

The exact values should be tuned after performance testing.

---

# 35. Docker Development Profiles

The project can support multiple Compose profiles.

Example:

```text
dev
test
```

Development:

```text
Frontend
Gateway
All required services
MySQL
Redis
Kafka
```

Testing may start only:

```text
MySQL
Redis
Kafka
```

while services are started by the test framework.

---

# 36. Local Development Architecture

A developer should be able to clone the repository and execute:

```bash
docker compose up -d
```

and obtain a working infrastructure environment.

The README should document:

1. Required Java version
2. Required Docker version
3. Environment variables
4. Startup commands
5. API Gateway URL
6. Frontend URL
7. Swagger/OpenAPI URLs
8. Default development credentials
9. Database initialization
10. Troubleshooting

---

# 37. Database Initialization

Database initialization scripts can be placed under:

```text
infrastructure/docker/mysql/
```

Example:

```text
infrastructure/
└── docker/
    └── mysql/
        ├── 01-create-databases.sql
        └── 02-seed-data.sql
```

Example databases:

```sql
CREATE DATABASE ewso_auth;
CREATE DATABASE ewso_employee;
CREATE DATABASE ewso_skill;
CREATE DATABASE ewso_project;
CREATE DATABASE ewso_task;
CREATE DATABASE ewso_workload;
CREATE DATABASE ewso_recommendation;
CREATE DATABASE ewso_performance;
CREATE DATABASE ewso_notification;
CREATE DATABASE ewso_analytics;
```

Production database schema management should use controlled migrations such as Flyway or Liquibase rather than relying on Docker initialization scripts.

---

# 38. Service Discovery in Docker

Eureka can still be used for service discovery.

Example:

```text
service-registry:8761
```

Services register themselves with:

```text
http://service-registry:8761/eureka
```

Because Docker provides DNS resolution, containers can also communicate using service names.

The architecture therefore has:

```text
Docker DNS
+
Eureka Service Discovery
```

Eureka provides application-level service discovery while Docker provides container networking.

---

# 39. Redis Failure Handling

Redis must not become a single point of failure for critical business operations.

If Redis is unavailable:

```text
Application
    |
    v
Redis unavailable
    |
    v
Fetch from database/service
    |
    v
Continue operation
```

Caching improves performance but should not be the source of truth.

The source of truth remains the appropriate service database.

---

# 40. Kafka Failure Handling

Kafka is used for asynchronous processing.

If Kafka is temporarily unavailable:

* retry event publication
* use appropriate producer configuration
* avoid duplicate business processing
* use idempotent consumers
* monitor failed events

Critical synchronous business operations should not depend unnecessarily on immediate Kafka availability.

---

# 41. Docker and Recommendation Engine

The recommendation engine runs inside:

```text
recommendation-service
```

It communicates with:

```text
employee-service
skill-service
task-service
workload-service
performance-service
```

Architecture:

```text
Recommendation Service
        |
        +--> Employee Service
        |
        +--> Skill Service
        |
        +--> Task Service
        |
        +--> Workload Service
        |
        +--> Performance Service
        |
        +--> Redis
        |
        +--> Kafka
```

The deterministic scoring engine remains independent from Docker infrastructure.

---

# 42. Docker and AI Engine

The AI task intelligence component can initially run inside the recommendation/task intelligence service.

Later it can become a separate container:

```text
ai-task-intelligence
```

Architecture:

```text
Task Service
      |
      v
AI Task Intelligence
      |
      v
Structured Task Understanding
      |
      v
Recommendation Service
```

If the external AI provider is unavailable, the deterministic fallback remains available.

---

# 43. Docker Security Boundaries

The production architecture should separate:

```text
Public Network
       |
       v
Load Balancer
       |
       v
API Gateway
       |
       v
Private Application Network
       |
       +--> Microservices
       |
       +--> Redis
       |
       +--> Kafka
       |
       +--> Database
```

Databases and infrastructure services should not be directly accessible from the public internet.

---

# 44. Local vs Production Docker

| Area           | Local           | Production                 |
| -------------- | --------------- | -------------------------- |
| Orchestration  | Docker Compose  | AWS ECS/other orchestrator |
| Database       | MySQL container | AWS RDS                    |
| Redis          | Redis container | AWS ElastiCache            |
| Kafka          | Local Kafka     | Managed/self-managed Kafka |
| Secrets        | `.env`          | AWS Secrets Manager        |
| Logs           | Docker logs     | CloudWatch                 |
| Networking     | Docker network  | AWS VPC                    |
| Load balancing | Optional        | AWS Load Balancer          |
| Scaling        | Manual          | Horizontal                 |
| TLS            | Optional local  | Required                   |
| Monitoring     | Basic           | Centralized                |
| Image registry | Local           | Container registry         |

Docker remains the packaging standard in both environments.

---

# 45. Production Deployment Principle

The same application artifact should move through environments.

```text
Developer Code
      |
      v
GitHub
      |
      v
CI Build
      |
      v
Docker Image
      |
      v
Container Registry
      |
      v
Test Environment
      |
      v
Production
```

The application image should not be rebuilt differently for each environment.

Configuration should change through environment variables/secrets.

---

# 46. Example Complete Compose Skeleton

A simplified root-level Compose file:

```yaml
services:

  mysql:
    image: mysql:8.4
    container_name: ewso-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - ewso-network

  redis:
    image: redis:7-alpine
    container_name: ewso-redis
    volumes:
      - redis_data:/data
    networks:
      - ewso-network

  kafka:
    image: apache/kafka:latest
    container_name: ewso-kafka
    networks:
      - ewso-network

  service-registry:
    build:
      context: ./backend/service-registry
    container_name: ewso-service-registry
    ports:
      - "8761:8761"
    networks:
      - ewso-network

  auth-service:
    build:
      context: ./backend/auth-service
    container_name: ewso-auth-service
    networks:
      - ewso-network

  employee-service:
    build:
      context: ./backend/employee-service
    container_name: ewso-employee-service
    networks:
      - ewso-network

  task-service:
    build:
      context: ./backend/task-service
    container_name: ewso-task-service
    networks:
      - ewso-network

  workload-service:
    build:
      context: ./backend/workload-service
    container_name: ewso-workload-service
    networks:
      - ewso-network

  recommendation-service:
    build:
      context: ./backend/recommendation-service
    container_name: ewso-recommendation-service
    networks:
      - ewso-network

  api-gateway:
    build:
      context: ./backend/api-gateway
    container_name: ewso-api-gateway
    ports:
      - "8080:8080"
    networks:
      - ewso-network

  web-app:
    build:
      context: ./frontend/web-app
    container_name: ewso-web-app
    ports:
      - "3000:80"
    networks:
      - ewso-network


volumes:
  mysql_data:
  redis_data:


networks:
  ewso-network:
    driver: bridge
```

This is a **structural baseline**, not the final production Compose file. Kafka configuration, all services, health checks, database schemas, and environment variables should be added as implementation progresses.

---

# 47. Docker Implementation Order

Docker implementation should be performed in this order:

### Phase 1 — Infrastructure

```text
MySQL
Redis
Kafka
```

### Phase 2 — Core platform

```text
Service Registry
API Gateway
Auth Service
```

### Phase 3 — Business services

```text
Employee
Skill
Project
Task
```

### Phase 4 — Intelligence

```text
Workload
Recommendation
Performance
```

### Phase 5 — Supporting services

```text
Notification
Analytics
```

### Phase 6 — Frontend

```text
React web-app
```

### Phase 7 — AI

```text
AI Task Intelligence
```

### Phase 8 — Production hardening

```text
Health checks
Resource limits
Security scanning
Secrets
Logging
Monitoring
Image versioning
```

---

# 48. Docker Definition of Done

Docker implementation is considered complete when:

* [ ] Every deployable Spring Boot service has a Dockerfile
* [ ] Frontend has a Dockerfile
* [ ] Multi-stage builds are implemented
* [ ] Runtime containers use JRE rather than JDK where appropriate
* [ ] Production containers do not run as root
* [ ] `.dockerignore` is configured
* [ ] Docker Compose starts required infrastructure
* [ ] MySQL has persistent storage
* [ ] Redis has persistent storage where required
* [ ] Kafka is containerized
* [ ] Docker network is configured
* [ ] Service-to-service communication uses container DNS/service names
* [ ] Environment variables are externalized
* [ ] Secrets are not committed
* [ ] Health checks are implemented
* [ ] Spring Boot Actuator is configured
* [ ] Database initialization is documented
* [ ] Kafka topics are documented
* [ ] Redis keys are documented
* [ ] Container logs are accessible
* [ ] Docker image naming/versioning is defined
* [ ] Images can be built reproducibly
* [ ] CI/CD can build the images
* [ ] Docker image security scanning is included in CI/CD
* [ ] Local environment can be started using Docker Compose
* [ ] Production deployment strategy is documented separately

---

# 49. Final Architecture Principle

Docker is the **packaging and execution layer**, not the business architecture.

The separation of responsibilities remains:

```text
React
  ↓
API Gateway
  ↓
Spring Boot Microservices
  ↓
Business Logic
  ↓
MySQL / Redis / Kafka
  ↓
AI Intelligence Layer
```

Docker packages these components into reproducible containers.

The core EWSO principles remain:

> **Business rules belong to the services.**

> **AI understands tasks but does not make the final assignment decision.**

> **Databases remain owned by their respective services.**

> **Redis is a cache, not the source of truth.**

> **Kafka handles asynchronous events, not every synchronous operation.**

> **Secrets belong outside Docker images.**

> **The same application image should be deployable across environments.**
