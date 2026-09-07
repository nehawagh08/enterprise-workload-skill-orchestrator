# API Documentation

## 1. Overview

The **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)** exposes RESTful APIs through the API Gateway.

The APIs support:

* Authentication
* Employee management
* Skill management
* Learning goals
* Project management
* Task/ticket management
* Workload management
* Employee recommendations
* Performance management
* Notifications
* Workforce analytics
* Audit operations

The API architecture follows:

```text
React Frontend
      │
      ↓
API Gateway
      │
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

---

# 2. API Standards

## Base URL

Local development:

```text
http://localhost:8080/api
```

Production:

```text
https://<enterprise-domain>/api
```

The frontend should communicate with the **API Gateway**, not directly with individual microservices.

---

# 3. API Versioning

APIs should be versioned.

Recommended format:

```text
/api/v1/...
```

Example:

```http
GET /api/v1/employees
```

This allows future versions to coexist.

Example:

```text
/api/v1/employees
/api/v2/employees
```

---

# 4. HTTP Methods

| Method | Purpose                           |
| ------ | --------------------------------- |
| GET    | Retrieve data                     |
| POST   | Create resource or execute action |
| PUT    | Replace/update resource           |
| PATCH  | Partially update resource         |
| DELETE | Delete/deactivate resource        |

---

# 5. Standard HTTP Status Codes

| Status | Meaning                                  |
| -----: | ---------------------------------------- |
|    200 | Successful request                       |
|    201 | Resource created                         |
|    204 | Successful request with no response body |
|    400 | Invalid request                          |
|    401 | Authentication required/failed           |
|    403 | Access denied                            |
|    404 | Resource not found                       |
|    409 | Conflict                                 |
|    422 | Business validation failure              |
|    429 | Too many requests                        |
|    500 | Internal server error                    |
|    503 | Service temporarily unavailable          |

---

# 6. Authentication

Authentication uses:

```text
Spring Security
+
JWT
+
RBAC
```

The client sends:

```http
Authorization: Bearer <JWT>
```

Example:

```http
GET /api/v1/employees/101
Authorization: Bearer eyJhbGciOiJIUzI1Ni...
```

---

# 7. Login API

## POST `/api/v1/auth/login`

Authenticates a user and returns a JWT.

### Request

```json
{
  "username": "employee101",
  "password": "password"
}
```

### Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1Ni...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 101,
  "role": "EMPLOYEE"
}
```

### Errors

```text
401 INVALID_CREDENTIALS
403 USER_INACTIVE
```

---

# 8. Employee APIs

Base:

```text
/api/v1/employees
```

---

## 8.1 Create Employee

```http
POST /api/v1/employees
```

### Authorization

```text
ADMIN
```

### Request

```json
{
  "employeeCode": "EMP-101",
  "firstName": "Rahul",
  "lastName": "Patil",
  "email": "rahul@example.com",
  "departmentId": 10,
  "designation": "Java Developer",
  "managerId": 25,
  "yearsOfExperience": 3.5
}
```

### Response

```json
{
  "employeeId": 101,
  "employeeCode": "EMP-101",
  "firstName": "Rahul",
  "lastName": "Patil",
  "email": "rahul@example.com",
  "departmentId": 10,
  "designation": "Java Developer",
  "yearsOfExperience": 3.5,
  "employmentStatus": "ACTIVE"
}
```

Status:

```text
201 Created
```

---

# 9. Get Employee

```http
GET /api/v1/employees/{employeeId}
```

Example:

```http
GET /api/v1/employees/101
```

### Response

```json
{
  "employeeId": 101,
  "employeeCode": "EMP-101",
  "name": "Rahul Patil",
  "email": "rahul@example.com",
  "department": "Engineering",
  "designation": "Java Developer",
  "managerId": 25,
  "yearsOfExperience": 3.5,
  "employmentStatus": "ACTIVE",
  "availabilityStatus": "AVAILABLE"
}
```

---

# 10. Search Employees

```http
GET /api/v1/employees
```

Supported filters:

```text
departmentId
designation
employmentStatus
availabilityStatus
managerId
skillId
page
size
sort
```

Example:

```http
GET /api/v1/employees?departmentId=10&employmentStatus=ACTIVE&page=0&size=20
```

### Response

```json
{
  "content": [
    {
      "employeeId": 101,
      "name": "Rahul Patil",
      "designation": "Java Developer",
      "employmentStatus": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

# 11. Update Employee

```http
PUT /api/v1/employees/{employeeId}
```

### Request

```json
{
  "designation": "Senior Java Developer",
  "managerId": 30,
  "yearsOfExperience": 4.0
}
```

### Response

```json
{
  "employeeId": 101,
  "message": "Employee updated successfully"
}
```

---

# 12. Update Employee Availability

```http
PATCH /api/v1/employees/{employeeId}/availability
```

### Request

```json
{
  "availabilityStatus": "PARTIALLY_AVAILABLE",
  "capacityHours": 20,
  "availableFrom": "2026-09-08T09:00:00",
  "availableUntil": "2026-09-08T18:00:00"
}
```

### Response

```json
{
  "employeeId": 101,
  "availabilityStatus": "PARTIALLY_AVAILABLE",
  "capacityHours": 20
}
```

This operation publishes:

```text
employee.availability.changed
```

---

# 13. Skill APIs

Base:

```text
/api/v1/skills
```

---

## 13.1 Get Skill Catalogue

```http
GET /api/v1/skills
```

Example:

```http
GET /api/v1/skills?category=FRAMEWORK
```

### Response

```json
{
  "content": [
    {
      "skillId": 1,
      "skillName": "Spring Boot",
      "category": "FRAMEWORK",
      "status": "ACTIVE"
    },
    {
      "skillId": 2,
      "skillName": "React",
      "category": "FRAMEWORK",
      "status": "ACTIVE"
    }
  ]
}
```

---

# 14. Create Skill

```http
POST /api/v1/skills
```

### Authorization

```text
ADMIN
```

### Request

```json
{
  "skillName": "Apache Kafka",
  "category": "MESSAGING",
  "description": "Distributed event streaming platform"
}
```

### Response

```json
{
  "skillId": 15,
  "skillName": "Apache Kafka",
  "category": "MESSAGING"
}
```

---

# 15. Get Employee Skills

```http
GET /api/v1/employees/{employeeId}/skills
```

### Response

```json
{
  "employeeId": 101,
  "skills": [
    {
      "skillId": 1,
      "skillName": "Java",
      "proficiency": 5,
      "yearsOfExperience": 3.5,
      "verified": true
    },
    {
      "skillId": 2,
      "skillName": "Spring Boot",
      "proficiency": 4,
      "yearsOfExperience": 2.8,
      "verified": true
    },
    {
      "skillId": 3,
      "skillName": "AWS",
      "proficiency": 3,
      "yearsOfExperience": 1.0,
      "verified": false
    }
  ]
}
```

---

# 16. Add Employee Skill

```http
POST /api/v1/employees/{employeeId}/skills
```

### Request

```json
{
  "skillId": 3,
  "proficiency": 3,
  "yearsOfExperience": 1.0,
  "verified": false
}
```

### Validation

```text
proficiency >= 1
proficiency <= 5
yearsOfExperience >= 0
```

---

# 17. Update Employee Skill

```http
PUT /api/v1/employees/{employeeId}/skills/{skillId}
```

### Request

```json
{
  "proficiency": 4,
  "yearsOfExperience": 1.8,
  "verified": true
}
```

This publishes:

```text
employee.skill.updated
```

---

# 18. Learning Goal APIs

Base:

```text
/api/v1/employees/{employeeId}/learning-goals
```

---

## Create Learning Goal

```http
POST /api/v1/employees/{employeeId}/learning-goals
```

### Request

```json
{
  "skillId": 3,
  "targetProficiency": 4,
  "priority": "HIGH",
  "targetDate": "2027-03-01"
}
```

### Response

```json
{
  "learningGoalId": 501,
  "employeeId": 101,
  "skillId": 3,
  "targetProficiency": 4,
  "priority": "HIGH",
  "status": "ACTIVE"
}
```

---

# 19. Project APIs

Base:

```text
/api/v1/projects
```

---

## Create Project

```http
POST /api/v1/projects
```

### Authorization

```text
ADMIN
PROJECT_MANAGER
```

### Request

```json
{
  "projectCode": "PAY-001",
  "projectName": "Enterprise Payment Platform",
  "description": "Payment processing platform",
  "projectManagerId": 25,
  "startDate": "2026-09-01",
  "endDate": "2027-03-31"
}
```

---

# 20. Get Project

```http
GET /api/v1/projects/{projectId}
```

### Response

```json
{
  "projectId": 501,
  "projectCode": "PAY-001",
  "projectName": "Enterprise Payment Platform",
  "status": "ACTIVE",
  "projectManagerId": 25,
  "startDate": "2026-09-01",
  "endDate": "2027-03-31"
}
```

---

# 21. Add Project Skill Requirement

```http
POST /api/v1/projects/{projectId}/skills
```

### Request

```json
{
  "skillId": 2,
  "minimumProficiency": 4,
  "importance": "MANDATORY"
}
```

---

# 22. Get Project Skills

```http
GET /api/v1/projects/{projectId}/skills
```

### Response

```json
{
  "projectId": 501,
  "skills": [
    {
      "skillName": "Java",
      "minimumProficiency": 4,
      "importance": "MANDATORY"
    },
    {
      "skillName": "Spring Boot",
      "minimumProficiency": 4,
      "importance": "MANDATORY"
    },
    {
      "skillName": "AWS",
      "minimumProficiency": 3,
      "importance": "IMPORTANT"
    }
  ]
}
```

---

# 23. Task APIs

Base:

```text
/api/v1/tasks
```

---

# 24. Create Task

```http
POST /api/v1/tasks
```

### Authorization

```text
MANAGER
TEAM_LEAD
PROJECT_MANAGER
```

### Request

```json
{
  "projectId": 501,
  "title": "Fix payment service timeout",
  "description": "Payment API is timing out while communicating with provider",
  "priority": "HIGH",
  "complexity": "COMPLEX",
  "estimatedHours": 6,
  "deadline": "2026-09-10T18:00:00",
  "requiredSkills": [
    {
      "skillId": 1,
      "minimumProficiency": 4,
      "importance": "MANDATORY"
    },
    {
      "skillId": 2,
      "minimumProficiency": 4,
      "importance": "MANDATORY"
    }
  ]
}
```

### Response

```json
{
  "taskId": "TASK-1024",
  "projectId": 501,
  "status": "OPEN",
  "message": "Task created successfully"
}
```

This publishes:

```text
task.created
```

---

# 25. Get Task

```http
GET /api/v1/tasks/{taskId}
```

Example:

```http
GET /api/v1/tasks/TASK-1024
```

### Response

```json
{
  "taskId": "TASK-1024",
  "title": "Fix payment service timeout",
  "projectId": 501,
  "priority": "HIGH",
  "complexity": "COMPLEX",
  "estimatedHours": 6,
  "deadline": "2026-09-10T18:00:00",
  "status": "OPEN",
  "assignedEmployeeId": null
}
```

---

# 26. Search Tasks

```http
GET /api/v1/tasks
```

Filters:

```text
projectId
status
priority
complexity
assignedEmployeeId
createdBy
deadlineFrom
deadlineTo
page
size
sort
```

Example:

```http
GET /api/v1/tasks?status=OPEN&priority=HIGH&page=0&size=20
```

---

# 27. Update Task

```http
PUT /api/v1/tasks/{taskId}
```

### Request

```json
{
  "title": "Fix payment provider timeout",
  "priority": "CRITICAL",
  "estimatedHours": 8,
  "deadline": "2026-09-09T18:00:00"
}
```

---

# 28. Update Task Status

```http
PATCH /api/v1/tasks/{taskId}/status
```

### Request

```json
{
  "status": "IN_PROGRESS"
}
```

Valid transitions should be controlled by business rules.

Example:

```text
OPEN
 ↓
IN_PROGRESS
 ↓
COMPLETED
```

or:

```text
IN_PROGRESS
 ↓
BLOCKED
 ↓
IN_PROGRESS
```

Invalid transitions should return:

```text
422 INVALID_TASK_STATE_TRANSITION
```

---

# 29. Assign Task Manually

```http
POST /api/v1/tasks/{taskId}/assign
```

### Request

```json
{
  "employeeId": 101,
  "assignmentType": "MANAGER_SELECTED"
}
```

### Response

```json
{
  "taskId": "TASK-1024",
  "employeeId": 101,
  "status": "ASSIGNED"
}
```

This publishes:

```text
task.assigned
```

---

# 30. Reassign Task

```http
POST /api/v1/tasks/{taskId}/reassign
```

### Request

```json
{
  "employeeId": 108,
  "reason": "Employee 108 has payment-domain experience"
}
```

### Response

```json
{
  "taskId": "TASK-1024",
  "previousEmployeeId": 101,
  "newEmployeeId": 108,
  "status": "REASSIGNED"
}
```

This should update assignment history rather than deleting the previous assignment.

---

# 31. Workload APIs

Base:

```text
/api/v1/workloads
```

---

# 32. Get Employee Workload

```http
GET /api/v1/workloads/employees/{employeeId}
```

### Response

```json
{
  "employeeId": 101,
  "availableHours": 40,
  "assignedHours": 24,
  "workloadPercentage": 60,
  "status": "NORMAL"
}
```

---

# 33. Get Team Workload

```http
GET /api/v1/workloads/teams/{teamId}
```

### Response

```json
{
  "teamId": 20,
  "totalCapacityHours": 400,
  "assignedHours": 280,
  "utilizationPercentage": 70,
  "employees": {
    "available": 4,
    "normal": 5,
    "busy": 2,
    "overloaded": 1
  }
}
```

---

# 34. Get Overloaded Employees

```http
GET /api/v1/workloads/overloaded
```

### Response

```json
{
  "employees": [
    {
      "employeeId": 108,
      "workloadPercentage": 94,
      "status": "OVERLOADED"
    }
  ]
}
```

---

# 35. Recommendation APIs

Base:

```text
/api/v1/recommendations
```

This is the core API group of EWSO.

---

# 36. Generate Recommendation

```http
POST /api/v1/recommendations/tasks/{taskId}
```

Example:

```http
POST /api/v1/recommendations/tasks/TASK-1024
```

### Response

```json
{
  "taskId": "TASK-1024",
  "generatedAt": "2026-09-07T10:30:00Z",
  "recommendations": [
    {
      "recommendationId": 9001,
      "employeeId": 101,
      "rank": 1,
      "score": 91.2,
      "confidence": 0.91,
      "status": "RECOMMENDED",
      "reason": "Strong Spring Boot and payment-domain experience"
    },
    {
      "recommendationId": 9002,
      "employeeId": 107,
      "rank": 2,
      "score": 84.7,
      "confidence": 0.87,
      "status": "RECOMMENDED",
      "reason": "Strong Java and microservices skills with moderate workload"
    }
  ]
}
```

---

# 37. Recommendation Factor Details

```http
GET /api/v1/recommendations/{recommendationId}
```

### Response

```json
{
  "recommendationId": 9001,
  "taskId": "TASK-1024",
  "employeeId": 101,
  "score": 91.2,
  "confidence": 0.91,
  "factors": [
    {
      "name": "SKILL_MATCH",
      "score": 95,
      "weight": 30,
      "contribution": 28.5,
      "explanation": "Strong Java and Spring Boot proficiency"
    },
    {
      "name": "EXPERIENCE",
      "score": 90,
      "weight": 20,
      "contribution": 18.0,
      "explanation": "Relevant payment integration experience"
    },
    {
      "name": "WORKLOAD",
      "score": 80,
      "weight": 20,
      "contribution": 16.0,
      "explanation": "Current workload is 60%"
    },
    {
      "name": "AVAILABILITY",
      "score": 100,
      "weight": 10,
      "contribution": 10.0,
      "explanation": "Employee is currently available"
    }
  ]
}
```

---

# 38. Accept Recommendation

```http
POST /api/v1/recommendations/{recommendationId}/accept
```

### Request

```json
{
  "managerId": 25
}
```

### Response

```json
{
  "recommendationId": 9001,
  "decision": "ACCEPTED",
  "selectedEmployeeId": 101,
  "message": "Recommendation accepted"
}
```

The system then triggers task assignment.

---

# 39. Reject Recommendation

```http
POST /api/v1/recommendations/{recommendationId}/reject
```

### Request

```json
{
  "managerId": 25,
  "reason": "Employee is required for production support"
}
```

### Response

```json
{
  "recommendationId": 9001,
  "decision": "REJECTED"
}
```

---

# 40. Manager Override

```http
POST /api/v1/recommendations/{recommendationId}/override
```

### Request

```json
{
  "managerId": 25,
  "selectedEmployeeId": 108,
  "reason": "Employee 108 has undocumented payment-domain experience"
}
```

### Response

```json
{
  "recommendationId": 9001,
  "decision": "OVERRIDDEN",
  "recommendedEmployeeId": 101,
  "selectedEmployeeId": 108,
  "reason": "Employee 108 has undocumented payment-domain experience"
}
```

Every override should be audited.

---

# 41. No Suitable Candidate Response

If no employee satisfies mandatory requirements:

```json
{
  "taskId": "TASK-1024",
  "status": "NO_SUITABLE_CANDIDATE",
  "message": "No eligible employee satisfies the mandatory skill and availability requirements",
  "missingSkills": [
    "Payment Systems",
    "Spring Boot"
  ]
}
```

The system must not recommend an unqualified employee merely because the employee has a learning goal.

---

# 42. Growth-Aware Recommendation

Future API:

```http
POST /api/v1/recommendations/tasks/{taskId}?includeGrowth=true
```

The response may include:

```json
{
  "employeeId": 115,
  "score": 82.5,
  "growthOpportunity": true,
  "growthSkill": "AWS",
  "currentProficiency": 3,
  "targetProficiency": 4,
  "reason": "Employee has an active AWS learning goal and sufficient baseline capability"
}
```

Growth-aware allocation must not override critical delivery requirements.

---

# 43. Performance APIs

Base:

```text
/api/v1/performance
```

---

## Get Employee Performance

```http
GET /api/v1/performance/employees/{employeeId}
```

### Response

```json
{
  "employeeId": 101,
  "tasksCompleted": 87,
  "onTimePercentage": 94.5,
  "averageCompletionHours": 5.2,
  "reopenRate": 3.4,
  "averageQualityScore": 92,
  "performanceScore": 91.7
}
```

---

# 44. Record Task Performance

```http
POST /api/v1/performance/tasks/{taskId}
```

### Request

```json
{
  "employeeId": 101,
  "estimatedHours": 6,
  "actualHours": 5.5,
  "completedOnTime": true,
  "reopened": false,
  "qualityScore": 94,
  "incidentRelated": true
}
```

---

# 45. Notifications APIs

Base:

```text
/api/v1/notifications
```

---

## Get Notifications

```http
GET /api/v1/notifications
```

### Response

```json
{
  "content": [
    {
      "notificationId": 501,
      "type": "TASK_ASSIGNED",
      "title": "New task assigned",
      "message": "TASK-1024 has been assigned to you",
      "isRead": false,
      "createdAt": "2026-09-07T10:35:00Z"
    }
  ]
}
```

---

# 46. Mark Notification as Read

```http
PATCH /api/v1/notifications/{notificationId}/read
```

### Response

```json
{
  "notificationId": 501,
  "isRead": true
}
```

---

# 47. Analytics APIs

Base:

```text
/api/v1/analytics
```

---

## Workforce Overview

```http
GET /api/v1/analytics/workforce
```

### Response

```json
{
  "totalEmployees": 1000,
  "activeEmployees": 972,
  "availableEmployees": 310,
  "busyEmployees": 420,
  "overloadedEmployees": 52,
  "averageWorkload": 67.4
}
```

---

# 48. Team Analytics

```http
GET /api/v1/analytics/teams/{teamId}
```

### Response

```json
{
  "teamId": 20,
  "teamName": "Payment Engineering",
  "employeeCount": 18,
  "openTasks": 27,
  "completedTasks": 143,
  "utilization": 74.5,
  "skillCoverage": 91.2
}
```

---

# 49. Skill Demand Analytics

```http
GET /api/v1/analytics/skills
```

### Response

```json
{
  "skills": [
    {
      "skillName": "Java",
      "demand": 342,
      "availableEmployees": 280,
      "gap": 62
    },
    {
      "skillName": "Kafka",
      "demand": 185,
      "availableEmployees": 97,
      "gap": 88
    }
  ]
}
```

---

# 50. Standard Error Response

Every service should return a consistent error structure.

```json
{
  "timestamp": "2026-09-07T10:30:00Z",
  "status": 404,
  "code": "EMPLOYEE_NOT_FOUND",
  "message": "Employee was not found",
  "path": "/api/v1/employees/999",
  "correlationId": "REQ-82A71"
}
```

---

# 51. Validation Error

For invalid request data:

```json
{
  "timestamp": "2026-09-07T10:30:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "errors": [
    {
      "field": "estimatedHours",
      "message": "must be greater than or equal to 0"
    },
    {
      "field": "priority",
      "message": "must be a valid priority"
    }
  ],
  "correlationId": "REQ-91AB2"
}
```

Spring Boot should implement this through centralized exception handling.

---

# 52. Business Error Codes

Recommended error codes:

```text
AUTHENTICATION_FAILED
INVALID_TOKEN
ACCESS_DENIED

EMPLOYEE_NOT_FOUND
EMPLOYEE_INACTIVE
EMPLOYEE_ON_LEAVE

SKILL_NOT_FOUND
DUPLICATE_EMPLOYEE_SKILL
INVALID_PROFICIENCY

PROJECT_NOT_FOUND
PROJECT_INACTIVE

TASK_NOT_FOUND
INVALID_TASK_STATE_TRANSITION
TASK_ALREADY_ASSIGNED
TASK_DEADLINE_EXPIRED

NO_SUITABLE_CANDIDATE
MANDATORY_SKILL_MISSING
EMPLOYEE_OVERLOADED

RECOMMENDATION_NOT_FOUND
RECOMMENDATION_EXPIRED
RECOMMENDATION_ALREADY_DECIDED

NOTIFICATION_NOT_FOUND

VALIDATION_ERROR
RESOURCE_CONFLICT
INTERNAL_ERROR
SERVICE_UNAVAILABLE
```

---

# 53. Pagination Standard

List APIs should use pagination.

Example:

```http
GET /api/v1/tasks?page=0&size=20&sort=deadline,asc
```

Standard response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 125,
  "totalPages": 7,
  "first": true,
  "last": false
}
```

Default:

```text
page = 0
size = 20
maximum size = 100
```

---

# 54. Filtering Standard

Example:

```http
GET /api/v1/tasks
    ?status=OPEN
    &priority=HIGH
    &projectId=501
    &page=0
    &size=20
```

Filtering should be performed at the database/query level rather than loading all records into application memory.

---

# 55. Sorting Standard

Example:

```http
GET /api/v1/employees?sort=yearsOfExperience,desc
```

Multiple sorting:

```http
GET /api/v1/tasks?sort=priority,desc&sort=deadline,asc
```

---

# 56. Correlation ID

Every request should have a correlation ID.

Example:

```http
X-Correlation-ID: REQ-82A71
```

If the client does not provide one, the API Gateway generates it.

The same ID should propagate through:

```text
Gateway
 ↓
Service
 ↓
Feign call
 ↓
Kafka event
```

This allows developers to trace a request across microservices.

---

# 57. Idempotency

Critical operations should support idempotent behavior.

For example:

```http
POST /api/v1/tasks/TASK-1024/assign
```

should not accidentally create duplicate assignments because the client retries the request.

Potential header:

```http
Idempotency-Key: 7c1e-82a1-9912
```

The service can use this for important commands such as:

```text
Task assignment
Task reassignment
Recommendation acceptance
Payment-like external operations
```

---

# 58. API Security Matrix

| API Area                | ADMIN | MANAGER | TEAM_LEAD |      PM | EMPLOYEE |
| ----------------------- | ----: | ------: | --------: | ------: | -------: |
| Employee Create         |     ✓ |         |           |         |          |
| Employee View           |     ✓ |       ✓ |         ✓ |       ✓ |      Own |
| Employee Update         |     ✓ | Limited |   Limited | Limited |      Own |
| Skill Catalogue         |     ✓ |       ✓ |         ✓ |       ✓ |        ✓ |
| Own Skills              |     ✓ |       ✓ |         ✓ |       ✓ |        ✓ |
| Project Create          |     ✓ |         |           |       ✓ |          |
| Task Create             |     ✓ |       ✓ |         ✓ |       ✓ |          |
| Task Assign             |     ✓ |       ✓ |         ✓ |       ✓ |          |
| Recommendations         |     ✓ |       ✓ |         ✓ |       ✓ |  Limited |
| Recommendation Override |     ✓ |       ✓ |         ✓ |       ✓ |          |
| Workforce Analytics     |     ✓ |       ✓ |         ✓ |       ✓ |  Limited |
| Audit Logs              |     ✓ | Limited |           |         |          |

Authorization must ultimately be enforced by backend services, not only by the frontend.

---

# 59. API Gateway Routing

Example Spring Cloud Gateway configuration concept:

```text
/api/v1/auth/**

        ↓

Auth Service
```

```text
/api/v1/employees/**

        ↓

Employee Service
```

```text
/api/v1/skills/**

        ↓

Skill Service
```

```text
/api/v1/projects/**

        ↓

Project Service
```

```text
/api/v1/tasks/**

        ↓

Task Service
```

```text
/api/v1/workloads/**

        ↓

Workload Service
```

```text
/api/v1/recommendations/**

        ↓

Recommendation Service
```

---

# 60. Service-to-Service APIs

Not every internal API needs to be exposed through the public gateway.

For example:

```text
Recommendation Service
        ↓
Internal Feign Client
        ↓
Skill Service
```

Internal endpoints can be separated from external APIs.

Example:

```http
GET /internal/employees/{id}/skills
```

This reduces unnecessary exposure.

---

# 61. OpenFeign Communication

Recommendation Service can use clients such as:

```java
@FeignClient(name = "employee-service")
public interface EmployeeClient {

    @GetMapping("/internal/employees/{id}")
    EmployeeResponse getEmployee(
        @PathVariable Long id
    );
}
```

Similarly:

```text
SkillClient
WorkloadClient
TaskClient
PerformanceClient
```

These clients should have:

* Timeouts
* Error handling
* Circuit breakers
* Logging
* Correlation ID propagation

---

# 62. Kafka and APIs

REST is used for immediate operations.

Kafka is used for asynchronous side effects.

Example:

```text
POST /tasks/TASK-1024/assign
          ↓
      Task Service
          ↓
      Database
          ↓
    task.assigned
          ↓
        Kafka
          ├── Workload Service
          ├── Notification Service
          └── Analytics Service
```

The API should return after the primary task operation succeeds.

Consumers process secondary effects asynchronously.

---

# 63. API and Recommendation Flow

Complete example:

```text
POST /api/v1/recommendations/tasks/TASK-1024
                ↓
       Recommendation Service
                ↓
       Get Task Information
                ↓
       Get Required Skills
                ↓
       Get Eligible Employees
                ↓
       Get Workload
                ↓
       Get Performance
                ↓
       Calculate Scores
                ↓
       Rank Candidates
                ↓
       Generate Explanation
                ↓
       Store Recommendation
                ↓
       Return Top Candidates
```

---

# 64. API and Assignment Flow

```text
Manager
   ↓
POST /recommendations/9001/accept
   ↓
Recommendation Service
   ↓
Record Decision
   ↓
Task Assignment Command
   ↓
Task Service
   ↓
Create Assignment
   ↓
task.assigned
   ↓
Kafka
   ├── Workload Service
   ├── Notification Service
   └── Analytics Service
```

---

# 65. OpenAPI / Swagger

Every Spring Boot service should expose OpenAPI documentation.

Recommended dependency:

```text
springdoc-openapi
```

Swagger UI should be available during development.

Example:

```text
/swagger-ui.html
```

or the project's configured OpenAPI UI path.

Each API should document:

* Endpoint
* HTTP method
* Authentication
* Request body
* Query parameters
* Response
* Error codes
* Example payloads

---

# 66. DTO Strategy

Entities should not be directly exposed through REST APIs.

Use DTOs.

Example:

```text
EmployeeEntity
      ↓
EmployeeResponseDTO
```

Request:

```text
CreateEmployeeRequest
```

Response:

```text
EmployeeResponse
```

This protects internal database structure and allows API contracts to evolve independently.

Recommended naming:

```text
CreateEmployeeRequest
UpdateEmployeeRequest
EmployeeResponse

CreateTaskRequest
UpdateTaskRequest
TaskResponse

RecommendationResponse
RecommendationFactorResponse
```

---

# 67. Validation Strategy

Use Jakarta Bean Validation.

Example:

```java
@NotBlank
private String title;

@NotNull
@Positive
private BigDecimal estimatedHours;
```

For proficiency:

```java
@Min(1)
@Max(5)
private Integer proficiency;
```

Validation should happen at multiple levels:

```text
API validation
      ↓
Business validation
      ↓
Database constraints
```

---

# 68. API Logging

Do not log sensitive information.

Log:

```text
timestamp
HTTP method
endpoint
status
response time
user ID
correlation ID
service
error code
```

Do not log:

```text
passwords
JWT tokens
secrets
database passwords
sensitive personal information
```

---

# 69. API Performance Requirements

Target:

```text
Normal CRUD APIs:
< 500 ms

Recommendation API:
< 2 seconds for normal team size

Cached reads:
significantly lower latency where appropriate
```

Large list operations must use:

```text
Pagination
Filtering
Database indexes
```

rather than retrieving entire datasets.

---

# 70. API Resilience

External service calls should use:

```text
Timeout
Retry
Circuit Breaker
Fallback
```

Example:

```text
Recommendation API
       ↓
Workload Service
       ↓
      timeout
       ↓
Circuit Breaker
       ↓
Fallback
```

A temporary Workload Service failure should not necessarily make the entire platform unavailable.

---

# 71. API Testing Strategy

Each API should have:

### Unit Tests

Test:

```text
Service logic
Validation
Scoring
Business rules
```

### Controller Tests

Test:

```text
HTTP status
Request validation
Response structure
Authorization
```

### Integration Tests

Test:

```text
API
 ↓
Service
 ↓
Database
```

### Contract Tests

Verify that service-to-service API contracts remain compatible.

### Testcontainers

Use Testcontainers for realistic:

```text
MySQL
Kafka
Redis
```

integration testing.

---

# 72. API Documentation Workflow

Whenever an API changes:

```text
Code
 ↓
OpenAPI specification
 ↓
Tests
 ↓
Documentation
 ↓
Git commit
```

Example commit:

```text
docs: update task assignment API
```

---

# 73. Future API Extensions

Future versions may add:

```text
/api/v1/skill-gaps
/api/v1/learning-recommendations
/api/v1/workforce-planning
/api/v1/knowledge/search
/api/v1/knowledge/questions
/api/v1/incidents
```

Potential integrations:

```text
Jira
GitHub
ServiceNow
Microsoft Teams
Slack
Enterprise Identity Provider
Learning Management System
```

---

# 74. Future Knowledge API

The future Knowledge Service may expose:

```http
POST /api/v1/knowledge/search
```

Example:

```json
{
  "query": "Why was circuit breaker added to payment service?",
  "projectId": 501
}
```

Response:

```json
{
  "answer": "Circuit breaker was introduced to prevent cascading failures when the payment provider becomes unavailable.",
  "sources": [
    {
      "type": "ARCHITECTURE_DOCUMENT",
      "reference": "payment-service-architecture"
    },
    {
      "type": "GIT_COMMIT",
      "reference": "commit-a81f92"
    }
  ]
}
```

This belongs to the future RAG/Knowledge phase and should not complicate the MVP API architecture.

---

# 75. Complete API Map

```text
AUTH
/api/v1/auth/login


EMPLOYEES
/api/v1/employees
/api/v1/employees/{id}
/api/v1/employees/{id}/availability
/api/v1/employees/{id}/skills
/api/v1/employees/{id}/learning-goals


SKILLS
/api/v1/skills
/api/v1/skills/{id}


/PROJECTS
/api/v1/projects
/api/v1/projects/{id}
/api/v1/projects/{id}/skills
/api/v1/projects/{id}/members


TASKS
/api/v1/tasks
/api/v1/tasks/{id}
/api/v1/tasks/{id}/status
/api/v1/tasks/{id}/assign
/api/v1/tasks/{id}/reassign


WORKLOAD
/api/v1/workloads/employees/{id}
/api/v1/workloads/teams/{id}
/api/v1/workloads/overloaded


RECOMMENDATIONS
/api/v1/recommendations/tasks/{taskId}
/api/v1/recommendations/{id}
/api/v1/recommendations/{id}/accept
/api/v1/recommendations/{id}/reject
/api/v1/recommendations/{id}/override


PERFORMANCE
/api/v1/performance/employees/{id}
/api/v1/performance/tasks/{taskId}


/NOTIFICATIONS
/api/v1/notifications
/api/v1/notifications/{id}/read


ANALYTICS
/api/v1/analytics/workforce
/api/v1/analytics/teams/{id}
/api/v1/analytics/skills
```

---

# 76. API Design Principles

EWSO APIs should follow these principles:

### 1. RESTful design

Use standard HTTP methods and resource-oriented URLs.

### 2. Versioning

Use `/api/v1`.

### 3. DTO isolation

Never expose persistence entities directly.

### 4. Consistent errors

Use standard error structures and error codes.

### 5. Pagination

All potentially large collections must support pagination.

### 6. Validation

Validate both syntactic and business requirements.

### 7. Security

Every protected API requires authentication and authorization.

### 8. Idempotency

Important commands should be safe against retries.

### 9. Observability

Every request should have a correlation ID.

### 10. Resilience

Service-to-service communication must tolerate temporary failures.

### 11. Explainability

Recommendation APIs should return the factors behind a recommendation.

### 12. Human control

AI recommendations remain advisory; managers retain final authority.

---

# 77. Final API Architecture

```text
                         React
                           │
                           ↓
                    API Gateway
                           │
                    JWT Validation
                           │
              ┌────────────┼─────────────┐
              ↓            ↓             ↓
          Employee       Task       Recommendation
           APIs          APIs             APIs
              │            │               │
              ↓            ↓               ↓
          Employee       Task       Recommendation
           Service       Service          Service
              │            │               │
              └────────────┼───────────────┘
                           │
                         Kafka
                           │
          ┌────────────────┼────────────────┐
          ↓                ↓                ↓
      Workload       Notification       Analytics
       Service          Service           Service
```

The API layer therefore becomes the contract between the React frontend and the enterprise backend while keeping individual microservices independently maintainable, secure, testable, and scalable.
