# Business Rules

**Project:** AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)
**Document:** Business Rules Specification
**Version:** 1.0
**Status:** Draft

---

# 1. Purpose

This document defines the business rules that govern how the Enterprise Workload & Skill Orchestrator makes decisions.

Business rules determine:

* Who is eligible for a task
* How employees are scored
* How workload affects recommendations
* How skill gaps are calculated
* When learning opportunities can influence allocation
* When managers can override recommendations
* How task priority affects assignment
* How performance affects future recommendations
* How recommendation decisions are explained

These rules represent the **business intelligence layer** of the application.

---

# 2. Core Business Principle

The system shall not simply assign a task to the employee with the highest technical skill.

The system shall identify the employee who provides the best overall balance between:

```text
Technical Capability
+
Availability
+
Workload
+
Experience
+
Performance
+
Task Complexity
+
Development Opportunity
```

Therefore:

> **Best candidate ≠ Highest skill candidate**

---

# 3. Business Rule Categories

The rules are divided into:

1. Employee Eligibility
2. Skill Matching
3. Workload
4. Availability
5. Task Priority
6. Task Complexity
7. Recommendation Scoring
8. Recommendation Ranking
9. Explainability
10. Manager Override
11. Growth-Aware Allocation
12. Performance
13. Skill Gap
14. Project Capacity
15. Task Lifecycle
16. Data Consistency
17. Notifications
18. Audit
19. AI Behavior

---

# 4. Employee Eligibility Rules

## BR-001 — Active Employee Only

Only active employees may be considered for new task recommendations.

```text
Employee Status = ACTIVE
```

Employees who are:

* Inactive
* Resigned
* Suspended

shall not be recommended.

---

## BR-002 — Leave Restriction

Employees currently on approved leave shall not be recommended for normal task assignment.

```text
Availability = ON_LEAVE
        ↓
Not Eligible
```

---

## BR-003 — Minimum Skill Requirement

If a task defines a mandatory skill and minimum proficiency, an employee must satisfy that requirement to be considered eligible.

Example:

```text
Task:
Spring Boot — Advanced

Employee:
Spring Boot — Beginner

Result:
NOT ELIGIBLE
```

---

## BR-004 — Missing Mandatory Skill

An employee missing a mandatory technical skill shall normally be excluded from the recommendation pool.

Example:

```text
Required:
Java
Spring Boot
Kafka

Employee:
Java
Spring Boot

Result:
Kafka missing
→ Not eligible
```

Optional skills may reduce the recommendation score without completely excluding the employee.

---

# 5. Skill Matching Rules

## BR-005 — Skill Match Calculation

Skill matching shall compare:

```text
Required Skill
        vs
Employee Skill
```

The system should consider:

* Skill presence
* Proficiency
* Years of experience
* Relevance to task
* Recency of usage

---

## BR-006 — Proficiency Levels

The initial proficiency scale shall be:

```text
1 → Beginner
2 → Basic
3 → Intermediate
4 → Advanced
5 → Expert
```

Higher proficiency indicates stronger technical capability.

---

## BR-007 — Skill Match Score

The system shall calculate a normalized skill-match score between:

```text
0 – 100
```

Example:

```text
Required:
Java — Advanced
Spring Boot — Advanced
Kafka — Intermediate

Employee:
Java — Expert
Spring Boot — Advanced
Kafka — Basic

Skill Match:
Approximately 80–90%
```

The exact implementation shall be defined in the recommendation-engine design.

---

## BR-008 — Multiple Skill Requirements

When a task requires multiple skills, the system shall consider all required skills rather than evaluating only the strongest skill.

Example:

```text
Task:
Java + Spring Boot + Kafka
```

A candidate with:

```text
Java: Expert
Spring Boot: Expert
Kafka: None
```

should not automatically outrank someone with:

```text
Java: Advanced
Spring Boot: Advanced
Kafka: Intermediate
```

---

# 6. Workload Rules

## BR-009 — Workload Calculation

Employee workload shall be calculated as:

```text
Workload % =
(Assigned Estimated Hours / Available Capacity Hours) × 100
```

Example:

```text
Capacity = 40 hours
Assigned = 24 hours

Workload = 60%
```

---

## BR-010 — Workload Classification

Initial workload classification:

| Workload | Status        |
| -------: | ------------- |
|    0–40% | AVAILABLE     |
|   41–70% | NORMAL        |
|   71–85% | BUSY          |
|  86–100% | OVERLOADED    |
|    >100% | OVER_CAPACITY |

These thresholds shall be configurable.

---

## BR-011 — Overloaded Employee Penalty

An overloaded employee may still technically qualify for a task, but their recommendation score shall receive a significant workload penalty.

Example:

```text
Employee A
Skill Match = 96%
Workload = 95%

Employee B
Skill Match = 89%
Workload = 45%
```

Employee B may be ranked higher because assigning additional work to Employee A creates delivery risk.

---

## BR-012 — Over-Capacity Restriction

Employees above 100% workload should normally not receive additional non-critical tasks.

Exception:

A manager may manually override this restriction.

The override shall be recorded in the audit trail.

---

# 7. Availability Rules

## BR-013 — Availability States

Employees shall have one of the following availability states:

```text
AVAILABLE
PARTIALLY_AVAILABLE
BUSY
ON_LEAVE
UNAVAILABLE
```

---

## BR-014 — Availability and Recommendation

Employees with higher available capacity should receive a higher availability score.

Example:

```text
Employee A → 20% workload
Employee B → 70% workload

A receives higher availability score.
```

---

## BR-015 — Upcoming Leave

If an employee has approved upcoming leave that conflicts with the task deadline, the system should reduce their recommendation score.

Example:

```text
Task Deadline:
Friday

Employee Leave:
Thursday–Monday

Result:
Significant availability penalty
```

---

# 8. Task Priority Rules

## BR-016 — Priority Levels

Tasks shall have:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

---

## BR-017 — Critical Task Rule

For CRITICAL tasks:

```text
Delivery Reliability
        >
Employee Learning Opportunity
```

Technical competence, experience, availability, and reliability shall receive higher importance.

---

## BR-018 — Critical Task Eligibility

A critical task should require stronger minimum eligibility.

Example:

```text
CRITICAL

Required:
Spring Boot — Advanced

Candidate:
Spring Boot — Beginner

Result:
Not recommended
```

---

## BR-019 — Low Priority Learning Opportunity

For LOW or selected MEDIUM priority tasks, the system may give additional weight to employee learning goals.

Example:

```text
Task:
Kafka integration

Employee:
Learning Goal = Kafka

Result:
Potential growth opportunity
```

---

# 9. Task Complexity Rules

## BR-020 — Complexity Levels

Tasks shall use:

```text
SIMPLE
MEDIUM
COMPLEX
EXPERT
```

---

## BR-021 — Complexity and Experience

Complex tasks should require employees with appropriate experience.

Example:

```text
Task:
EXPERT

Employee:
0.5 years experience

Result:
Low suitability
```

---

## BR-022 — Complexity Mismatch

An employee with insufficient experience may be excluded from complex tasks when the risk is high.

For low-risk tasks, the system may recommend a less experienced employee as a learning opportunity.

---

# 10. Recommendation Scoring Rules

## BR-023 — Overall Recommendation Score

The initial scoring model shall be:

```text
Overall Score =
Skill Match × 30%
+
Experience × 20%
+
Workload × 20%
+
Task Complexity × 10%
+
Availability × 10%
+
Performance × 5%
+
Learning Opportunity × 5%
```

Total:

```text
100%
```

---

# 11. Score Normalization

## BR-024 — Score Range

Every factor shall be normalized to:

```text
0 – 100
```

The final recommendation score shall also be:

```text
0 – 100
```

Example:

```text
Skill Match       = 92
Experience        = 88
Workload          = 75
Complexity        = 90
Availability      = 80
Performance       = 94
Learning          = 60
```

The weighted result produces the final recommendation score.

---

# 12. Recommendation Example

## BR-025 — Example Candidate Evaluation

Task:

```text
Fix Spring Boot payment-service timeout
```

Requirements:

```text
Java
Spring Boot
Microservices
Redis
AWS
```

Candidate A:

```text
Skill Match       = 95
Experience        = 90
Workload          = 45
Complexity        = 90
Availability      = 90
Performance       = 92
Learning          = 50
```

Candidate B:

```text
Skill Match       = 88
Experience        = 85
Workload          = 25
Complexity        = 85
Availability      = 95
Performance       = 90
Learning          = 85
```

The system evaluates all factors rather than selecting Candidate A purely because of the higher skill score.

---

# 13. Candidate Ranking Rules

## BR-026 — Ranking

Candidates shall be sorted by final recommendation score in descending order.

```text
Rank 1 → 92.4
Rank 2 → 88.7
Rank 3 → 83.1
```

---

## BR-027 — Minimum Recommendation Threshold

The system should support a configurable minimum recommendation threshold.

Example:

```text
Score >= 70
→ Recommended

Score < 70
→ Weak Match
```

The threshold shall be configurable by administrators.

---

## BR-028 — No Suitable Candidate

If no employee satisfies the minimum recommendation threshold:

```text
No Strong Candidate Available
```

The system shall not falsely claim that an unsuitable employee is a strong match.

The manager may:

* Search manually
* Expand the team
* Reassign workload
* Adjust requirements
* Override the recommendation

---

# 14. Tie-Breaking Rules

## BR-029 — Equal Scores

If two employees have nearly identical scores, the system shall use tie-breakers.

Initial tie-break order:

```text
1. Higher skill match
2. Lower workload
3. Higher relevant experience
4. Higher performance
5. Earlier availability
```

---

# 15. Recommendation Explanation Rules

## BR-030 — Explain Positive Factors

The system must identify the factors that increased the recommendation score.

Example:

```text
+ Strong Spring Boot experience
+ Previous payment-system experience
+ Low workload
+ High completion rate
```

---

## BR-031 — Explain Negative Factors

The system should identify factors that reduced the score.

Example:

```text
- Limited Kafka experience
- Upcoming leave
- High current workload
```

---

## BR-032 — Explain Ranking Difference

The system should be capable of explaining why Candidate A ranked above Candidate B.

Example:

```text
Employee A ranked higher because:

+ 15% stronger Spring Boot match
+ 20% lower workload
+ Higher payment-domain experience
```

---

# 16. Manager Override Rules

## BR-033 — Human Decision Authority

The manager has final authority over task assignment.

The AI recommendation is advisory.

```text
AI
 ↓
Recommendation
 ↓
Manager Review
 ↓
Final Assignment
```

---

## BR-034 — Manual Assignment

A manager may manually assign a task to any eligible employee.

---

## BR-035 — Override of Recommendation

A manager may choose an employee ranked below the recommended candidate.

The system should capture:

```text
Recommended Employee
Selected Employee
Manager
Timestamp
Override Reason
```

---

## BR-036 — Override Reason

For high-risk or critical tasks, an override reason should be mandatory.

Example:

```text
Override Reason:
Employee has prior client-specific knowledge.
```

---

# 17. Growth-Aware Allocation

## BR-037 — Learning Goal Matching

If:

```text
Employee Learning Goal
        =
Task Required Skill
```

the system may increase the learning-opportunity score.

---

## BR-038 — Minimum Skill for Growth

An employee must have a minimum baseline skill before being recommended for a learning opportunity.

Example:

```text
Task:
Kafka — Intermediate

Employee:
Kafka — Beginner
```

The system may consider the employee only for low-risk tasks.

---

## BR-039 — No Skill + Critical Task

An employee with no relevant skill shall not receive a critical task merely because the task matches their learning goal.

```text
Learning Goal
      ≠
Eligibility
```

Learning opportunity can influence ranking only after minimum eligibility requirements are satisfied.

---

## BR-040 — Growth Risk Control

Growth-aware allocation shall consider:

```text
Task Priority
Task Complexity
Employee Skill
Employee Experience
Deadline
Employee Workload
```

Learning should never create unacceptable delivery risk.

---

# 18. Performance Rules

## BR-041 — Performance Score

Employee performance may consider:

```text
On-Time Completion
+
Average Completion Time
+
Quality
+
Reopened Tasks
+
Incident Resolution
```

These metrics shall be normalized before being included in recommendations.

---

## BR-042 — Performance History

Recent performance should have greater relevance than very old performance.

The exact time window will be configurable.

---

## BR-043 — Insufficient Performance Data

New employees may not have enough historical performance data.

The system shall not unfairly penalize them because of missing historical data.

Possible approach:

```text
Missing Performance Data
        ↓
Neutral Score
        ↓
Do not apply artificial penalty
```

---

# 19. Skill Gap Rules

## BR-044 — Project Skill Gap

A skill gap exists when project requirements exceed currently available team capability.

Example:

```text
Required:
5 Java developers

Available:
3 Java developers

Gap:
2 developers
```

---

## BR-045 — Skill Gap Severity

Initial classification:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Severity may depend on:

* Number of missing resources
* Project deadline
* Skill criticality
* Current team capacity

---

## BR-046 — Training Candidate Identification

When a skill gap exists, the system may identify employees who:

* Have adjacent skills
* Have beginner-level knowledge
* Have relevant learning goals
* Have available capacity

These employees can be considered potential training candidates.

---

# 20. Project Capacity Rules

## BR-047 — Project Capacity

The system should calculate:

```text
Required Effort
vs
Available Team Capacity
```

Example:

```text
Project Work:
500 hours

Available Capacity:
420 hours

Capacity Gap:
80 hours
```

---

## BR-048 — Capacity Warning

The system should warn managers when project demand exceeds available capacity.

---

# 21. Task Assignment Rules

## BR-049 — One Primary Assignee

Each task shall have one primary assignee in the initial version.

Future versions may support multiple assignees.

---

## BR-050 — Assignment and Workload

When a task is assigned:

```text
Task Assignment
       ↓
Employee Assigned Hours +
       ↓
Workload Recalculated
```

---

## BR-051 — Task Reassignment

When a task is reassigned:

```text
Old Employee
     ↓
Assigned Hours Removed

New Employee
     ↓
Assigned Hours Added
```

Workload must be recalculated.

---

# 22. Task Lifecycle Rules

## BR-052 — Valid State Transition

Only valid task state transitions shall be allowed.

Example:

```text
CREATED
   ↓
ASSIGNED
   ↓
IN_PROGRESS
   ↓
COMPLETED
```

Invalid transitions shall be rejected.

---

## BR-053 — Blocked Task

A task may be marked:

```text
BLOCKED
```

when work cannot continue due to dependency, technical issue, external requirement, or other approved reason.

---

## BR-054 — Reopened Task

A completed task may be reopened if additional work is required.

The system should preserve the original completion history.

---

# 23. Event Rules

## BR-055 — Task Creation Event

When a task is created:

```text
task.created
```

shall be published.

---

## BR-056 — Assignment Event

When a task is assigned:

```text
task.assigned
```

shall be published.

This event may trigger:

```text
Workload Update
Notification
Audit Record
Analytics Update
```

---

## BR-057 — Completion Event

When a task is completed:

```text
task.completed
```

shall be published.

This may trigger:

```text
Workload Recalculation
Performance Update
Analytics Update
```

---

# 24. Notification Rules

## BR-058 — New Assignment

An employee should receive a notification when a task is assigned.

---

## BR-059 — Deadline Warning

The system should notify employees when a task approaches its deadline.

---

## BR-060 — Overdue Task

If the deadline passes while the task remains incomplete:

```text
Task Status ≠ COMPLETED
+
Current Time > Deadline
```

the task becomes overdue.

Relevant users should be notified.

---

# 25. Audit Rules

## BR-061 — Assignment Audit

Every assignment shall be auditable.

---

## BR-062 — Recommendation Audit

Recommendation generation should record:

```text
Task
Candidates
Scores
Recommendation
Timestamp
Algorithm Version
```

---

## BR-063 — Configuration Audit

Changes to recommendation weights or important business configuration shall be audited.

Example:

```text
Old:
Skill Match = 30%

New:
Skill Match = 35%
```

The system shall record who made the change and when.

---

# 26. AI Rules

## BR-064 — AI Is Advisory

AI shall provide recommendations and analysis but shall not make irreversible employee decisions autonomously in the initial versions.

---

## BR-065 — Deterministic Baseline

The initial recommendation engine must have a deterministic scoring model.

This provides:

```text
Consistency
+
Debuggability
+
Testability
+
Explainability
```

---

## BR-066 — LLM as Enhancement

Future LLM capabilities may enhance:

* Task classification
* Skill extraction
* Task summarization
* Explanation generation
* Project knowledge search

The LLM shall not replace core business rules.

---

## BR-067 — AI Fallback

If an external AI service becomes unavailable:

```text
AI Service
    ↓
Unavailable
    ↓
Fallback Logic
    ↓
Core Workflow Continues
```

The task management system should remain operational.

---

# 27. Recommendation Safety Rules

## BR-068 — No False Confidence

The system should not report high confidence when insufficient data exists.

Example:

```text
Employee has no skill history
+
No performance history
+
No availability data
```

The recommendation confidence should be reduced.

---

## BR-069 — Missing Data

Missing data shall be handled explicitly.

The system must distinguish between:

```text
Skill = 0
```

and:

```text
Skill information unavailable
```

These represent different situations.

---

## BR-070 — Critical Delivery Protection

No recommendation rule may prioritize employee learning over successful delivery of a critical task.

This is one of the core principles of EWSO.

---

# 28. Data Freshness Rules

## BR-071 — Workload Freshness

Recommendation calculations should use current workload information.

Stale workload data should not be used when reliable current information is available.

---

## BR-072 — Skill Freshness

Skill profiles should contain a last-updated or last-used indicator.

A skill not used for a long period may receive a recency penalty.

---

# 29. Configuration Rules

## BR-073 — Configurable Scoring Weights

Recommendation weights shall be configurable.

Initial configuration:

```text
Skill Match          30%
Experience           20%
Workload             20%
Complexity           10%
Availability         10%
Performance           5%
Learning Opportunity  5%
```

---

## BR-074 — Weight Validation

The sum of recommendation weights must equal:

```text
100%
```

The system shall reject invalid configurations.

Example:

```text
30 + 20 + 20 + 10 + 10 + 5 + 10
= 105%

→ Invalid
```

---

# 30. Conflict Resolution

## BR-075 — Skill vs Workload Conflict

When a highly skilled employee is overloaded and a moderately skilled employee is available, the system shall evaluate delivery risk rather than automatically selecting the most skilled employee.

---

## BR-076 — Experience vs Learning Conflict

An experienced employee may rank higher for critical tasks.

A less experienced employee may rank higher for suitable low-risk growth opportunities.

---

## BR-077 — Manager vs AI Conflict

When a manager overrides an AI recommendation:

```text
Manager Decision
        >
AI Recommendation
```

The system shall respect the manager's final decision while preserving the recommendation history.

---

# 31. Core Decision Hierarchy

When evaluating a candidate, the system should follow this conceptual hierarchy:

```text
                Task Requirements
                       ↓
                Eligibility Check
                       ↓
              Delivery Risk Check
                       ↓
          Skill & Experience Evaluation
                       ↓
             Workload Evaluation
                       ↓
             Availability Evaluation
                       ↓
           Performance Evaluation
                       ↓
          Growth Opportunity Evaluation
                       ↓
              Final Recommendation
                       ↓
               Manager Decision
```

---

# 32. Golden Rules of EWSO

The following principles are fundamental to the system.

### Rule 1 — Do Not Assign Blindly

The system should use employee and task information rather than simple manual matching.

### Rule 2 — Skills Alone Are Not Enough

Technical skill is important, but workload, availability, experience, and performance also matter.

### Rule 3 — Protect Delivery

Critical business tasks should prioritize successful delivery over employee development.

### Rule 4 — AI Must Be Explainable

Managers should understand why an employee was recommended.

### Rule 5 — Manager Has Final Authority

AI recommendations are advisory.

### Rule 6 — Learning Must Be Controlled

Employee development can influence allocation only when delivery risk is acceptable.

### Rule 7 — Preserve History

Recommendations, overrides, assignments, and important decisions should be auditable.

### Rule 8 — Missing Data Is Not Zero

Unknown information must not automatically be interpreted as poor capability.

### Rule 9 — Current Workload Matters

A highly skilled employee who is already overloaded may not be the best candidate.

### Rule 10 — Improve Through Feedback

Historical assignment and performance data should eventually be used to improve recommendation quality.

---

# 33. Business Decision Example

Consider:

```text
Task:
Fix payment-service timeout

Priority:
HIGH

Complexity:
MEDIUM
```

Three candidates:

| Factor       | Employee A | Employee B | Employee C |
| ------------ | ---------: | ---------: | ---------: |
| Skill Match  |         95 |         88 |         82 |
| Experience   |         92 |         85 |         80 |
| Workload     |         90 |         40 |         25 |
| Availability |         30 |         90 |         95 |
| Performance  |         95 |         90 |         85 |
| Learning     |         40 |         80 |         90 |

A simplistic system might select:

```text
Employee A
```

because they have the highest technical skill.

EWSO instead evaluates the complete context.

Employee A may have:

```text
Excellent technical capability
BUT
Very high workload
AND
Low availability
```

Therefore Employee B could become the preferred candidate.

This demonstrates the central principle:

> **EWSO optimizes for the best overall assignment, not the highest individual skill.**

---

# 34. Future Business Intelligence

Future versions may introduce additional rules for:

* Team dependency risk
* Client-specific expertise
* Domain expertise
* Historical task similarity
* Incident severity
* Project profitability
* SLA risk
* Employee burnout indicators
* Predicted task completion time
* Organizational skill forecasting
* Automated resource planning

These rules are outside the initial MVP.

---

# 35. Business Rules Implementation Principle

Business rules should not be scattered throughout controllers and database repositories.

The recommendation logic should be implemented as a dedicated business domain/service layer.

Conceptually:

```text
REST Controller
       ↓
Recommendation Service
       ↓
Eligibility Rules
       ↓
Skill Matching
       ↓
Workload Rules
       ↓
Availability Rules
       ↓
Performance Rules
       ↓
Growth Rules
       ↓
Scoring Engine
       ↓
Ranking Engine
       ↓
Explanation Builder
```

This architecture makes the rules:

* Testable
* Maintainable
* Configurable
* Explainable
* Independently evolvable

---

# 36. Acceptance Criteria

The business-rule implementation shall be considered successful when:

* Employees with mandatory missing skills are not incorrectly recommended.
* Employees on leave are excluded from normal recommendations.
* Workload affects recommendation ranking.
* Critical tasks prioritize delivery reliability.
* Learning goals can influence suitable low-risk tasks.
* Managers can override recommendations.
* Recommendation explanations identify positive and negative factors.
* Recommendation scores are reproducible for identical inputs.
* Recommendation weights total 100%.
* Assignment updates employee workload.
* Task completion updates workload and performance information.
* Important business decisions are auditable.
* AI failure does not stop core task management.
* The system never treats missing information as automatically equivalent to zero capability.

---

# 37. Final Business Rule Model

The EWSO decision model can be summarized as:

```text
                    TASK
                     |
          +----------+----------+
          |                     |
    Requirements             Context
          |                     |
       Skills              Priority
       Complexity           Deadline
       Experience           Effort
                              |
                              ↓
                    ELIGIBILITY ENGINE
                              |
                              ↓
                    CANDIDATE POOL
                              |
          +-------------------+-------------------+
          |                   |                   |
       Skills             Workload          Availability
          |                   |                   |
          +-------------------+-------------------+
                              |
                         Performance
                              |
                       Learning Goals
                              |
                              ↓
                    SCORING ENGINE
                              |
                              ↓
                    RANKING ENGINE
                              |
                              ↓
                 EXPLAINABLE RESULTS
                              |
                              ↓
                       MANAGER REVIEW
                              |
                  +-----------+-----------+
                  |                       |
                ACCEPT                  OVERRIDE
                  |                       |
                  +-----------+-----------+
                              |
                              ↓
                       TASK ASSIGNED
                              |
                              ↓
                     WORKLOAD UPDATED
                              |
                              ↓
                     PERFORMANCE DATA
                              |
                              ↓
                   FUTURE RECOMMENDATIONS
```

This creates the foundation for the EWSO **closed-loop intelligence model**.
