# Database Design

## 1. Overview

The **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)** uses a relational database architecture based on **MySQL**.

The database is designed around the microservice boundaries defined in the system architecture.

The primary objectives are:

* Maintain strong data integrity
* Clearly define service ownership
* Avoid unnecessary duplication
* Support efficient employee/task searches
* Support workload calculations
* Store recommendation results and explanations
* Maintain complete assignment history
* Support auditability
* Enable future analytics and AI capabilities
* Scale as the number of employees, projects, and tasks increases

---

# 2. Database Architecture

For local development and the initial portfolio implementation, a single MySQL server can host multiple logical databases.

```text
MySQL Server
│
├── ewso_auth
├── ewso_employee
├── ewso_skill
├── ewso_project
├── ewso_task
├── ewso_workload
├── ewso_recommendation
├── ewso_performance
├── ewso_notification
└── ewso_analytics
```

Each microservice owns its corresponding database.

```text
Auth Service
     ↓
ewso_auth

Employee Service
     ↓
ewso_employee

Skill Service
     ↓
ewso_skill

Project Service
     ↓
ewso_project

Task Service
     ↓
ewso_task

Workload Service
     ↓
ewso_workload

Recommendation Service
     ↓
ewso_recommendation
```

A production deployment may eventually move these databases to independently managed database instances.

---

# 3. Database Ownership Principle

Each service owns its data.

For example:

```text
Employee Service
    → employees

Skill Service
    → skills
    → employee_skills

Task Service
    → tasks
    → task_skills
    → task_assignments
```

A service must **not directly modify another service's tables**.

For example:

```text
❌ Recommendation Service
      ↓
   UPDATE employees
```

Instead:

```text
Recommendation Service
      ↓
Employee Service API
```

or:

```text
Employee Service
      ↓
employee.updated
      ↓
Kafka
      ↓
Recommendation Service
```

---

# 4. Naming Conventions

## Database Names

```text
ewso_<service>
```

Example:

```text
ewso_employee
```

## Table Names

Use:

```text
snake_case
```

Examples:

```text
employee_skills
task_assignments
learning_goals
recommendation_factors
```

## Primary Keys

Use:

```text
BIGINT UNSIGNED
```

for internal database identifiers.

External business identifiers can use human-readable values.

Example:

```text
Internal ID:
101

Business ID:
EMP-101
```

---

# 5. Common Columns

Most business tables should contain:

```text
id
created_at
updated_at
```

Example:

```sql
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
```

Where appropriate:

```text
created_by
updated_by
```

should also be stored for audit-sensitive operations.

---

# 6. Auth Database

Database:

```text
ewso_auth
```

Tables:

```text
users
roles
user_roles
```

---

## 6.1 users

Stores authentication information.

| Column        | Type         | Constraints      | Description            |
| ------------- | ------------ | ---------------- | ---------------------- |
| id            | BIGINT       | PK               | Internal identifier    |
| username      | VARCHAR(100) | UNIQUE, NOT NULL | Login username         |
| email         | VARCHAR(255) | UNIQUE, NOT NULL | Login email            |
| password_hash | VARCHAR(255) | NOT NULL         | Hashed password        |
| employee_id   | BIGINT       | NOT NULL         | Associated employee    |
| status        | VARCHAR(30)  | NOT NULL         | ACTIVE/LOCKED/INACTIVE |
| created_at    | TIMESTAMP    | NOT NULL         | Creation time          |
| updated_at    | TIMESTAMP    | NOT NULL         | Last update            |

Important:

`password_hash` must never contain plaintext passwords.

---

## 6.2 roles

```text
roles
```

| Column      | Type         | Constraints      |
| ----------- | ------------ | ---------------- |
| id          | BIGINT       | PK               |
| role_name   | VARCHAR(50)  | UNIQUE, NOT NULL |
| description | VARCHAR(255) | NULL             |

Initial roles:

```text
ADMIN
MANAGER
TEAM_LEAD
PROJECT_MANAGER
EMPLOYEE
```

---

## 6.3 user_roles

Many-to-many relationship between users and roles.

| Column  | Type   | Constraints |
| ------- | ------ | ----------- |
| user_id | BIGINT | PK, FK      |
| role_id | BIGINT | PK, FK      |

Primary key:

```text
(user_id, role_id)
```

---

# 7. Employee Database

Database:

```text
ewso_employee
```

Tables:

```text
departments
employees
employee_availability
```

---

# 8. departments

Stores organizational departments.

| Column          | Type         | Constraints      |
| --------------- | ------------ | ---------------- |
| id              | BIGINT       | PK               |
| department_code | VARCHAR(50)  | UNIQUE           |
| department_name | VARCHAR(150) | UNIQUE, NOT NULL |
| status          | VARCHAR(30)  | NOT NULL         |
| created_at      | TIMESTAMP    | NOT NULL         |
| updated_at      | TIMESTAMP    | NOT NULL         |

Examples:

```text
Engineering
Cloud Operations
Data Engineering
Quality Engineering
Information Security
```

---

# 9. employees

Core employee profile.

| Column              | Type         | Constraints      |
| ------------------- | ------------ | ---------------- |
| id                  | BIGINT       | PK               |
| employee_code       | VARCHAR(50)  | UNIQUE, NOT NULL |
| first_name          | VARCHAR(100) | NOT NULL         |
| last_name           | VARCHAR(100) | NOT NULL         |
| email               | VARCHAR(255) | UNIQUE, NOT NULL |
| department_id       | BIGINT       | NOT NULL         |
| designation         | VARCHAR(100) | NOT NULL         |
| manager_id          | BIGINT       | NULL             |
| years_of_experience | DECIMAL(4,1) | NOT NULL         |
| employment_status   | VARCHAR(30)  | NOT NULL         |
| joining_date        | DATE         | NULL             |
| created_at          | TIMESTAMP    | NOT NULL         |
| updated_at          | TIMESTAMP    | NOT NULL         |

`manager_id` references another employee logically within the Employee Service.

Example:

```text
Employee 101
Manager → Employee 25
```

---

# 10. employee_availability

Stores current and scheduled availability.

| Column              | Type         | Constraints |
| ------------------- | ------------ | ----------- |
| id                  | BIGINT       | PK          |
| employee_id         | BIGINT       | NOT NULL    |
| availability_status | VARCHAR(30)  | NOT NULL    |
| available_from      | DATETIME     | NOT NULL    |
| available_until     | DATETIME     | NULL        |
| capacity_hours      | DECIMAL(6,2) | NOT NULL    |
| leave_start         | DATE         | NULL        |
| leave_end           | DATE         | NULL        |
| created_at          | TIMESTAMP    | NOT NULL    |
| updated_at          | TIMESTAMP    | NOT NULL    |

Availability states:

```text
AVAILABLE
PARTIALLY_AVAILABLE
BUSY
ON_LEAVE
UNAVAILABLE
```

---

# 11. Employee Indexes

Important indexes:

```sql
INDEX idx_employee_department (department_id)

INDEX idx_employee_manager (manager_id)

INDEX idx_employee_status (employment_status)

INDEX idx_employee_designation (designation)

INDEX idx_employee_email (email)
```

These support queries such as:

```text
Find all active Java developers
Find employees under a manager
Find available employees
Find employees in Engineering
```

---

# 12. Skill Database

Database:

```text
ewso_skill
```

Tables:

```text
skills
employee_skills
learning_goals
```

---

# 13. skills

Master skill catalogue.

| Column         | Type         | Constraints      |
| -------------- | ------------ | ---------------- |
| id             | BIGINT       | PK               |
| skill_name     | VARCHAR(150) | UNIQUE, NOT NULL |
| skill_category | VARCHAR(100) | NOT NULL         |
| description    | TEXT         | NULL             |
| status         | VARCHAR(30)  | NOT NULL         |
| created_at     | TIMESTAMP    | NOT NULL         |
| updated_at     | TIMESTAMP    | NOT NULL         |

Examples:

```text
Java
Spring Boot
Microservices
Kafka
Redis
AWS
MySQL
React
Python
Docker
Kubernetes
```

Skill categories:

```text
PROGRAMMING_LANGUAGE
FRAMEWORK
DATABASE
CLOUD
DEVOPS
MESSAGING
SECURITY
DOMAIN
TOOL
```

---

# 14. employee_skills

Stores an employee's skill profile.

| Column              | Type         | Constraints |
| ------------------- | ------------ | ----------- |
| id                  | BIGINT       | PK          |
| employee_id         | BIGINT       | NOT NULL    |
| skill_id            | BIGINT       | NOT NULL    |
| proficiency_level   | TINYINT      | NOT NULL    |
| years_of_experience | DECIMAL(4,1) | NOT NULL    |
| verified            | BOOLEAN      | NOT NULL    |
| last_used_date      | DATE         | NULL        |
| created_at          | TIMESTAMP    | NOT NULL    |
| updated_at          | TIMESTAMP    | NOT NULL    |

Proficiency:

```text
1 → Beginner
2 → Basic
3 → Intermediate
4 → Advanced
5 → Expert
```

Unique constraint:

```text
(employee_id, skill_id)
```

An employee should not have duplicate entries for the same skill.

---

# 15. learning_goals

Stores skills an employee wants to develop.

| Column             | Type        | Constraints |
| ------------------ | ----------- | ----------- |
| id                 | BIGINT      | PK          |
| employee_id        | BIGINT      | NOT NULL    |
| skill_id           | BIGINT      | NOT NULL    |
| target_proficiency | TINYINT     | NOT NULL    |
| priority           | VARCHAR(30) | NOT NULL    |
| target_date        | DATE        | NULL        |
| status             | VARCHAR(30) | NOT NULL    |
| created_at         | TIMESTAMP   | NOT NULL    |
| updated_at         | TIMESTAMP   | NOT NULL    |

Example:

```text
Employee:
101

Current AWS:
Intermediate

Learning Goal:
AWS Advanced

Target Date:
2027-03-01
```

This information can later be used by the growth-aware recommendation engine.

---

# 16. Project Database

Database:

```text
ewso_project
```

Tables:

```text
projects
project_skills
project_members
```

---

# 17. projects

| Column             | Type         | Constraints      |
| ------------------ | ------------ | ---------------- |
| id                 | BIGINT       | PK               |
| project_code       | VARCHAR(50)  | UNIQUE, NOT NULL |
| project_name       | VARCHAR(200) | NOT NULL         |
| description        | TEXT         | NULL             |
| project_manager_id | BIGINT       | NOT NULL         |
| start_date         | DATE         | NOT NULL         |
| end_date           | DATE         | NULL             |
| status             | VARCHAR(30)  | NOT NULL         |
| created_at         | TIMESTAMP    | NOT NULL         |
| updated_at         | TIMESTAMP    | NOT NULL         |

Project status:

```text
PLANNED
ACTIVE
ON_HOLD
COMPLETED
CANCELLED
```

---

# 18. project_skills

Stores skills required by a project.

| Column              | Type        | Constraints |
| ------------------- | ----------- | ----------- |
| id                  | BIGINT      | PK          |
| project_id          | BIGINT      | NOT NULL    |
| skill_id            | BIGINT      | NOT NULL    |
| minimum_proficiency | TINYINT     | NOT NULL    |
| importance          | VARCHAR(30) | NOT NULL    |
| created_at          | TIMESTAMP   | NOT NULL    |

Example:

```text
Project:
Enterprise Payment Platform

Skill:
Spring Boot

Minimum proficiency:
4

Importance:
MANDATORY
```

---

# 19. project_members

Stores project membership.

| Column                | Type         | Constraints |
| --------------------- | ------------ | ----------- |
| id                    | BIGINT       | PK          |
| project_id            | BIGINT       | NOT NULL    |
| employee_id           | BIGINT       | NOT NULL    |
| project_role          | VARCHAR(100) | NOT NULL    |
| allocation_percentage | DECIMAL(5,2) | NOT NULL    |
| start_date            | DATE         | NOT NULL    |
| end_date              | DATE         | NULL        |
| status                | VARCHAR(30)  | NOT NULL    |

Unique constraint:

```text
(project_id, employee_id)
```

---

# 20. Task Database

Database:

```text
ewso_task
```

Tables:

```text
tasks
task_skills
task_assignments
```

---

# 21. tasks

The central task/ticket table.

| Column          | Type         | Constraints      |
| --------------- | ------------ | ---------------- |
| id              | BIGINT       | PK               |
| task_code       | VARCHAR(50)  | UNIQUE, NOT NULL |
| project_id      | BIGINT       | NOT NULL         |
| title           | VARCHAR(255) | NOT NULL         |
| description     | TEXT         | NOT NULL         |
| priority        | VARCHAR(30)  | NOT NULL         |
| complexity      | VARCHAR(30)  | NOT NULL         |
| estimated_hours | DECIMAL(6,2) | NOT NULL         |
| actual_hours    | DECIMAL(6,2) | NULL             |
| deadline        | DATETIME     | NULL             |
| status          | VARCHAR(30)  | NOT NULL         |
| created_by      | BIGINT       | NOT NULL         |
| created_at      | TIMESTAMP    | NOT NULL         |
| updated_at      | TIMESTAMP    | NOT NULL         |

Example:

```text
TASK-1024
```

---

# 22. task_skills

Stores skills required for an individual task.

| Column              | Type        | Constraints |
| ------------------- | ----------- | ----------- |
| id                  | BIGINT      | PK          |
| task_id             | BIGINT      | NOT NULL    |
| skill_id            | BIGINT      | NOT NULL    |
| minimum_proficiency | TINYINT     | NOT NULL    |
| importance          | VARCHAR(30) | NOT NULL    |
| source              | VARCHAR(30) | NOT NULL    |
| created_at          | TIMESTAMP   | NOT NULL    |

`source` can indicate:

```text
MANUAL
AI
PROJECT_TEMPLATE
IMPORTED
```

This allows the system to distinguish between manually entered and AI-extracted requirements.

---

# 23. task_assignments

Stores assignment history.

| Column          | Type        | Constraints |
| --------------- | ----------- | ----------- |
| id              | BIGINT      | PK          |
| task_id         | BIGINT      | NOT NULL    |
| employee_id     | BIGINT      | NOT NULL    |
| assigned_by     | BIGINT      | NOT NULL    |
| assignment_type | VARCHAR(30) | NOT NULL    |
| assigned_at     | DATETIME    | NOT NULL    |
| unassigned_at   | DATETIME    | NULL        |
| status          | VARCHAR(30) | NOT NULL    |
| override_reason | TEXT        | NULL        |

Assignment types:

```text
AI_RECOMMENDED
MANAGER_SELECTED
MANUAL
REASSIGNED
```

This table is extremely important.

It allows the system to answer:

```text
Who was assigned?

Who assigned them?

Was the AI recommendation accepted?

Was the recommendation overridden?

How many times was the task reassigned?
```

---

# 24. Workload Database

Database:

```text
ewso_workload
```

Tables:

```text
employee_capacity
employee_workloads
```

---

# 25. employee_capacity

Stores employee capacity.

| Column          | Type         | Constraints |
| --------------- | ------------ | ----------- |
| id              | BIGINT       | PK          |
| employee_id     | BIGINT       | NOT NULL    |
| capacity_date   | DATE         | NOT NULL    |
| available_hours | DECIMAL(6,2) | NOT NULL    |
| reserved_hours  | DECIMAL(6,2) | NOT NULL    |
| leave_hours     | DECIMAL(6,2) | NOT NULL    |
| created_at      | TIMESTAMP    | NOT NULL    |
| updated_at      | TIMESTAMP    | NOT NULL    |

Unique constraint:

```text
(employee_id, capacity_date)
```

---

# 26. employee_workloads

Stores calculated workload snapshots.

| Column              | Type         | Constraints |
| ------------------- | ------------ | ----------- |
| id                  | BIGINT       | PK          |
| employee_id         | BIGINT       | NOT NULL    |
| calculation_date    | DATE         | NOT NULL    |
| available_hours     | DECIMAL(6,2) | NOT NULL    |
| assigned_hours      | DECIMAL(6,2) | NOT NULL    |
| workload_percentage | DECIMAL(6,2) | NOT NULL    |
| workload_status     | VARCHAR(30)  | NOT NULL    |
| calculated_at       | DATETIME     | NOT NULL    |

Formula:

```text
Workload %
=
Assigned Hours / Available Capacity × 100
```

Example:

```text
Assigned = 24
Capacity = 40

Workload = 60%
Status = NORMAL
```

---

# 27. Recommendation Database

Database:

```text
ewso_recommendation
```

Tables:

```text
recommendations
recommendation_factors
recommendation_decisions
```

---

# 28. recommendations

Stores recommendation results.

| Column                | Type         | Constraints |
| --------------------- | ------------ | ----------- |
| id                    | BIGINT       | PK          |
| task_id               | BIGINT       | NOT NULL    |
| employee_id           | BIGINT       | NOT NULL    |
| score                 | DECIMAL(5,2) | NOT NULL    |
| confidence            | DECIMAL(5,2) | NOT NULL    |
| rank_position         | INT          | NOT NULL    |
| recommendation_status | VARCHAR(30)  | NOT NULL    |
| generated_at          | DATETIME     | NOT NULL    |
| expires_at            | DATETIME     | NULL        |

Example:

```text
Task:
TASK-1024

Employee:
101

Score:
91.20

Confidence:
91%

Rank:
1
```

---

# 29. recommendation_factors

Stores why the employee received the score.

| Column            | Type         | Constraints |
| ----------------- | ------------ | ----------- |
| id                | BIGINT       | PK          |
| recommendation_id | BIGINT       | NOT NULL    |
| factor_name       | VARCHAR(100) | NOT NULL    |
| factor_score      | DECIMAL(5,2) | NOT NULL    |
| weight            | DECIMAL(5,2) | NOT NULL    |
| contribution      | DECIMAL(7,3) | NOT NULL    |
| explanation       | TEXT         | NOT NULL    |

Example:

```text
Factor:
Skill Match

Score:
95

Weight:
30%

Contribution:
28.5

Explanation:
Strong Java and Spring Boot proficiency.
```

This makes the recommendation **explainable**.

---

# 30. recommendation_decisions

Stores manager decisions.

| Column               | Type        | Constraints |
| -------------------- | ----------- | ----------- |
| id                   | BIGINT      | PK          |
| recommendation_id    | BIGINT      | NOT NULL    |
| manager_id           | BIGINT      | NOT NULL    |
| decision             | VARCHAR(30) | NOT NULL    |
| selected_employee_id | BIGINT      | NULL        |
| reason               | TEXT        | NULL        |
| decided_at           | DATETIME    | NOT NULL    |

Decisions:

```text
ACCEPTED
REJECTED
OVERRIDDEN
```

Example:

```text
AI Recommendation:
Employee 101

Manager selected:
Employee 108

Reason:
Employee 108 has undocumented payment-domain experience.
```

---

# 31. Performance Database

Database:

```text
ewso_performance
```

Tables:

```text
task_performance
employee_performance_summary
manager_feedback
```

---

# 32. task_performance

| Column            | Type         | Constraints |
| ----------------- | ------------ | ----------- |
| id                | BIGINT       | PK          |
| task_id           | BIGINT       | NOT NULL    |
| employee_id       | BIGINT       | NOT NULL    |
| estimated_hours   | DECIMAL(6,2) | NOT NULL    |
| actual_hours      | DECIMAL(6,2) | NOT NULL    |
| completed_on_time | BOOLEAN      | NOT NULL    |
| reopened          | BOOLEAN      | NOT NULL    |
| quality_score     | DECIMAL(5,2) | NULL        |
| incident_related  | BOOLEAN      | NOT NULL    |
| completed_at      | DATETIME     | NULL        |
| created_at        | TIMESTAMP    | NOT NULL    |

---

# 33. employee_performance_summary

Stores aggregated performance information.

| Column                   | Type         | Constraints |
| ------------------------ | ------------ | ----------- |
| id                       | BIGINT       | PK          |
| employee_id              | BIGINT       | UNIQUE      |
| tasks_completed          | INT          | NOT NULL    |
| on_time_percentage       | DECIMAL(5,2) | NOT NULL    |
| average_completion_hours | DECIMAL(8,2) | NOT NULL    |
| reopen_rate              | DECIMAL(5,2) | NOT NULL    |
| average_quality_score    | DECIMAL(5,2) | NULL        |
| performance_score        | DECIMAL(5,2) | NOT NULL    |
| calculated_at            | DATETIME     | NOT NULL    |

The recommendation engine can use `performance_score` rather than recalculating all historical tasks for every recommendation.

---

# 34. manager_feedback

| Column      | Type      | Constraints |
| ----------- | --------- | ----------- |
| id          | BIGINT    | PK          |
| employee_id | BIGINT    | NOT NULL    |
| manager_id  | BIGINT    | NOT NULL    |
| task_id     | BIGINT    | NULL        |
| rating      | TINYINT   | NOT NULL    |
| comments    | TEXT      | NULL        |
| created_at  | TIMESTAMP | NOT NULL    |

Rating:

```text
1–5
```

---

# 35. Notification Database

Database:

```text
ewso_notification
```

Table:

```text
notifications
```

---

# 36. notifications

| Column            | Type         | Constraints |
| ----------------- | ------------ | ----------- |
| id                | BIGINT       | PK          |
| employee_id       | BIGINT       | NOT NULL    |
| notification_type | VARCHAR(50)  | NOT NULL    |
| title             | VARCHAR(255) | NOT NULL    |
| message           | TEXT         | NOT NULL    |
| reference_type    | VARCHAR(50)  | NULL        |
| reference_id      | VARCHAR(100) | NULL        |
| is_read           | BOOLEAN      | NOT NULL    |
| created_at        | DATETIME     | NOT NULL    |
| read_at           | DATETIME     | NULL        |

Example:

```text
Type:
TASK_ASSIGNED

Title:
New task assigned

Message:
TASK-1024 has been assigned to you.
```

---

# 37. Analytics Database

The Analytics Service should eventually maintain read-optimized data instead of repeatedly querying operational databases.

Database:

```text
ewso_analytics
```

Potential tables:

```text
employee_workload_daily
team_workload_daily
skill_demand_daily
task_metrics_daily
project_capacity_daily
```

These tables can be populated from Kafka events.

Example:

```text
task.completed
       ↓
Kafka
       ↓
Analytics Service
       ↓
Daily Metrics
```

This keeps analytics workloads separate from transactional workloads.

---

# 38. Audit Database

Audit information may initially be maintained by the relevant service or by a dedicated audit mechanism.

Recommended table:

```text
audit_logs
```

Possible database:

```text
ewso_audit
```

| Column         | Type         | Constraints |
| -------------- | ------------ | ----------- |
| id             | BIGINT       | PK          |
| actor_id       | BIGINT       | NOT NULL    |
| action         | VARCHAR(100) | NOT NULL    |
| entity_type    | VARCHAR(100) | NOT NULL    |
| entity_id      | VARCHAR(100) | NOT NULL    |
| old_value      | JSON         | NULL        |
| new_value      | JSON         | NULL        |
| reason         | TEXT         | NULL        |
| correlation_id | VARCHAR(100) | NULL        |
| created_at     | DATETIME     | NOT NULL    |

Examples:

```text
TASK_ASSIGNED
TASK_REASSIGNED
RECOMMENDATION_OVERRIDDEN
EMPLOYEE_UPDATED
SKILL_UPDATED
```

---

# 39. Cross-Service Relationships

Because each microservice owns its database, traditional database foreign keys should generally **not cross service boundaries**.

For example:

```text
Task Service
tasks.project_id
```

references a Project Service entity logically.

However:

```text
❌ FOREIGN KEY → ewso_project.projects(id)
```

should not be used across independent service databases.

Instead:

```text
Task Service
    ↓
project_id
    ↓
Project Service API
```

This maintains service independence.

---

# 40. Logical Entity Relationships

At the business level:

```text
Department
    │
    └── Employee
           │
           ├── Employee Skills
           │       │
           │       └── Skill
           │
           ├── Learning Goals
           │
           ├── Availability
           │
           ├── Workload
           │
           └── Performance

Project
    │
    ├── Project Skills
    │
    ├── Project Members
    │
    └── Tasks
           │
           ├── Task Skills
           │
           └── Assignments
                  │
                  └── Employee

Task
    │
    └── Recommendation
           │
           ├── Recommendation Factors
           │
           └── Manager Decision
```

---

# 41. Simplified ER Diagram

```text
┌──────────────┐
│ Departments  │
└──────┬───────┘
       │
       │ 1:N
       ↓
┌──────────────┐
│  Employees   │
└──────┬───────┘
       │
       ├───────────────┐
       │               │
       ↓               ↓
┌──────────────┐ ┌──────────────┐
│EmployeeSkills│ │LearningGoals │
└──────┬───────┘ └──────┬───────┘
       │                │
       └───────┬────────┘
               ↓
        ┌─────────────┐
        │   Skills    │
        └─────────────┘


┌──────────────┐
│   Projects   │
└──────┬───────┘
       │
       ├──────────────┐
       ↓              ↓
┌──────────────┐ ┌──────────────┐
│ProjectSkills │ │ProjectMembers│
└──────────────┘ └──────┬───────┘
                        ↓
                    Employees


┌──────────────┐
│    Tasks     │
└──────┬───────┘
       │
       ├───────────────┐
       ↓               ↓
┌──────────────┐ ┌──────────────┐
│  TaskSkills  │ │TaskAssignments│
└──────────────┘ └──────┬───────┘
                        ↓
                    Employees
       │
       ↓
┌──────────────────────┐
│  Recommendations     │
└──────────┬───────────┘
           │
     ┌─────┴───────────┐
     ↓                 ↓
┌──────────────┐ ┌──────────────────┐
│Recommendation│ │Recommendation    │
│Factors       │ │Decisions         │
└──────────────┘ └──────────────────┘
```

---

# 42. Recommendation Data Model

The recommendation model is intentionally separated into three levels:

```text
Recommendation
      │
      ├── Factors
      │
      └── Decision
```

Example:

```text
Recommendation
TASK-1024 → Employee 101
Score = 91.2
Confidence = 0.91

        ↓

Factors
├── Skill Match = 95
├── Experience = 90
├── Workload = 80
├── Complexity = 90
├── Availability = 100
├── Performance = 88
└── Learning Opportunity = 75

        ↓

Manager Decision
OVERRIDDEN
Selected Employee = 108
Reason = Domain experience
```

This allows the system to explain **why** a recommendation was produced and **what the manager eventually decided**.

---

# 43. Workload Data Model

Workload should not be stored as a manually entered percentage.

It should be derived from capacity and assigned effort.

```text
Employee
   ↓
Capacity
   ↓
Assigned Tasks
   ↓
Assigned Hours
   ↓
Workload Calculation
   ↓
Workload Status
```

Example:

```text
Capacity = 40 hours

Task A = 8
Task B = 6
Task C = 10

Assigned = 24

Workload = 24 / 40 × 100
         = 60%
```

---

# 44. Task Assignment History

A task should not have only one `employee_id` column.

Instead:

```text
tasks
   │
   ↓
task_assignments
```

This preserves history.

Example:

```text
TASK-1024

10:00 → Employee 101
14:00 → Employee 108
16:00 → Employee 108 completed
```

The system can therefore answer:

```text
Who initially received the task?

Why was it reassigned?

Was the original assignment AI-generated?

Who performed the final work?
```

---

# 45. Indexing Strategy

Indexes should be added according to actual query patterns.

Important indexes include:

### Employee

```text
employee_code
email
department_id
manager_id
employment_status
```

### Skills

```text
skill_name
employee_id
skill_id
(employee_id, skill_id)
```

### Tasks

```text
task_code
project_id
status
priority
deadline
created_at
```

### Assignments

```text
task_id
employee_id
assigned_at
status
```

### Workload

```text
employee_id
calculation_date
workload_status
```

### Recommendations

```text
task_id
employee_id
rank_position
score
generated_at
```

---

# 46. Composite Index Examples

Some important queries require composite indexes.

Example:

```sql
INDEX idx_employee_skill
(employee_id, skill_id)
```

Useful for:

```text
Find whether employee 101 has Java.
```

Another:

```sql
INDEX idx_task_status_priority
(status, priority)
```

Useful for:

```text
Find open critical tasks.
```

Another:

```sql
INDEX idx_workload_status
(workload_status, calculation_date)
```

Useful for:

```text
Find currently overloaded employees.
```

Indexes should be added based on observed query patterns rather than indexing every column.

---

# 47. Data Integrity Rules

The database should enforce basic integrity wherever possible.

Examples:

```text
employee_code must be unique

email must be unique

skill_name must be unique

(employee_id, skill_id) must be unique

(project_id, employee_id) must be unique

(employee_id, capacity_date) must be unique
```

Proficiency validation:

```text
1 ≤ proficiency_level ≤ 5
```

Rating validation:

```text
1 ≤ rating ≤ 5
```

Hours must not be negative:

```text
estimated_hours >= 0
actual_hours >= 0
available_hours >= 0
assigned_hours >= 0
```

---

# 48. Transaction Boundaries

Transactions should be handled within a service's own database.

Example:

```text
Task Service Transaction

Create Task
   ↓
Create Task Skills
   ↓
Commit
```

If the transaction fails:

```text
Rollback
```

Cross-service transactions should not rely on distributed database transactions.

Instead, use:

```text
Kafka events
+
Idempotent consumers
+
Retry
+
Compensation where required
```

---

# 49. Soft Delete Strategy

For important business records, hard deletion should generally be avoided.

Instead:

```text
status = INACTIVE
```

or:

```text
deleted_at
```

Example:

```text
Employee
ACTIVE
INACTIVE
```

This preserves historical references.

Tasks, assignments, recommendations, and audit records should generally remain available for historical analysis.

---

# 50. Historical Data

Historical data is important for EWSO because recommendations can improve when historical outcomes are available.

Examples:

```text
Past assignments
Past completion times
Past workload
Past performance
Past recommendation scores
Manager overrides
Task outcomes
```

This allows future analytics such as:

```text
Which recommendation factors produce successful assignments?

How often do managers override recommendations?

Which skills correlate with successful task completion?

Which employees consistently complete certain task types efficiently?
```

---

# 51. AI Data Strategy

The initial version does not need a separate AI database.

AI-generated information can initially be stored in the relevant domain.

Example:

```text
task_skills.source = AI
```

Future AI-specific metadata can be introduced:

```text
ai_task_analysis
```

Potential fields:

```text
task_id
model_name
model_version
extracted_skills
predicted_complexity
predicted_domain
confidence
generated_at
```

This allows AI results to be versioned and audited.

---

# 52. Future Vector Database

The future Knowledge Service may introduce a vector database.

Example architecture:

```text
Jira
GitHub
Documents
Incident Reports
API Docs
     ↓
Knowledge Service
     ↓
Chunking
     ↓
Embeddings
     ↓
Vector Database
     ↓
RAG
     ↓
Enterprise Knowledge Assistant
```

The vector database should not replace MySQL.

MySQL remains responsible for transactional business data.

The vector database handles semantic retrieval.

---

# 53. Data Consistency Model

The platform uses two consistency approaches.

### Strong consistency

Used inside a service for:

```text
Task creation
Assignment transaction
Employee update
Skill update
Recommendation decision
```

### Eventual consistency

Used across services for:

```text
Workload updates
Analytics
Notifications
Performance summaries
Search indexes
```

Example:

```text
Task completed
      ↓
Task DB updated
      ↓
task.completed event
      ↓
Kafka
      ↓
Workload updated
      ↓
Performance updated
      ↓
Analytics updated
```

A short delay between these updates is acceptable.

---

# 54. Database Migration

Database schema changes should be version controlled.

Recommended technology:

```text
Flyway
```

Example:

```text
db/migration/
│
├── V1__create_departments.sql
├── V2__create_employees.sql
├── V3__create_skills.sql
├── V4__create_employee_skills.sql
├── V5__create_projects.sql
├── V6__create_tasks.sql
└── V7__create_recommendations.sql
```

Every schema change should be committed to Git.

---

# 55. Development Database

Local development can use Docker Compose:

```text
Docker Compose
      │
      ↓
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

This makes the entire project reproducible for another developer.

---

# 56. Production Database Strategy

A future AWS deployment can use:

```text
Application
     ↓
AWS Load Balancer
     ↓
Microservices
     ↓
Amazon RDS for MySQL
```

For caching:

```text
Microservices
     ↓
Redis / ElastiCache
```

For event processing:

```text
Microservices
     ↓
Kafka
```

Database backups, monitoring, encryption, and access control should be configured at the infrastructure level.

---

# 57. Security Considerations

Sensitive information must be protected.

Requirements:

* Passwords stored only as hashes
* No plaintext credentials
* Database credentials stored outside source code
* Secrets provided through environment variables or secret management
* Restricted database users
* Least-privilege access
* TLS for production connections
* Audit sensitive operations
* Avoid storing unnecessary personal information

Each service should have only the database permissions it requires.

---

# 58. Example End-to-End Data Flow

Consider:

> "Fix Spring Boot payment-service timeout."

### Step 1 — Task creation

Task Service stores:

```text
TASK-1024
Priority = HIGH
Complexity = COMPLEX
Estimated Hours = 6
```

### Step 2 — Required skills

```text
Java
Spring Boot
Microservices
Payment Systems
```

stored in:

```text
task_skills
```

### Step 3 — Recommendation request

Recommendation Service retrieves:

```text
Employee skills
Employee experience
Employee workload
Employee availability
Performance
Learning goals
```

### Step 4 — Recommendation

```text
Employee 101 → 91.2
Employee 107 → 84.7
Employee 115 → 79.4
```

### Step 5 — Manager decision

Manager selects Employee 101.

### Step 6 — Assignment

Task Service creates:

```text
task_assignments
```

### Step 7 — Event

```text
task.assigned
```

is published to Kafka.

### Step 8 — Workload

Workload Service updates:

```text
assigned_hours
workload_percentage
```

### Step 9 — Notification

Notification Service creates:

```text
TASK_ASSIGNED
```

### Step 10 — Analytics

Analytics Service updates task and workforce metrics.

---

# 59. Database Design Principles

The EWSO database follows these principles:

### 1. Service ownership

Each microservice owns its data.

### 2. Referential integrity

Relationships inside a service are enforced using database constraints.

### 3. No cross-service database coupling

Services communicate through APIs and events.

### 4. Historical traceability

Important business decisions remain auditable.

### 5. Explainability

Recommendation factors are stored separately.

### 6. Scalability

Indexes and read-optimized analytics models support growing workloads.

### 7. Eventual consistency

Kafka is used where real-time transactional consistency is unnecessary.

### 8. Security

Sensitive information is minimized and protected.

### 9. Migration control

All schema changes are version controlled through Flyway.

### 10. AI separation

AI-generated information enhances the transactional model without replacing it.

---

# 60. Final Logical Data Architecture

```text
                         ┌────────────────────┐
                         │    Auth Database    │
                         │ users / roles       │
                         └────────────────────┘

                         ┌────────────────────┐
                         │ Employee Database  │
                         │ employees          │
                         │ departments        │
                         │ availability       │
                         └─────────┬──────────┘
                                   │
                         ┌─────────▼──────────┐
                         │   Skill Database   │
                         │ skills             │
                         │ employee_skills    │
                         │ learning_goals     │
                         └─────────┬──────────┘
                                   │
              ┌────────────────────┴──────────────────┐
              │                                       │
      ┌───────▼────────┐                     ┌────────▼────────┐
      │ Project DB     │                     │ Task Database   │
      │ projects       │                     │ tasks           │
      │ project_skills │                     │ task_skills     │
      │ members        │                     │ assignments     │
      └────────────────┘                     └────────┬────────┘
                                                       │
                           ┌───────────────────────────┼─────────────────┐
                           │                           │                 │
                   ┌───────▼────────┐          ┌───────▼────────┐ ┌──────▼──────┐
                   │ Workload DB    │          │ Recommendation │ │ Performance │
                   │ capacity       │          │ DB             │ │ DB          │
                   │ workloads      │          │ recommendations│ │ metrics     │
                   └────────────────┘          │ factors        │ │ feedback    │
                                               │ decisions      │ └─────────────┘
                                               └────────────────┘

                           ┌───────────────────────┐
                           │ Notification Database │
                           │ notifications         │
                           └───────────────────────┘

                           ┌───────────────────────┐
                           │ Analytics Database    │
                           │ read-optimized data   │
                           └───────────────────────┘
```

---

# 61. Target Database Outcome

The database should support the following complete business journey:

```text
Employee
   ↓
Skills + Experience + Learning Goals
   ↓
Project
   ↓
Task
   ↓
Required Skills
   ↓
Recommendation Engine
   ↓
Candidate Ranking
   ↓
Manager Decision
   ↓
Task Assignment
   ↓
Workload Update
   ↓
Task Completion
   ↓
Performance Update
   ↓
Analytics
   ↓
Future Recommendations
```

This creates a **closed-loop enterprise workforce intelligence system** rather than a simple employee/task management application.
