# Recommendation Engine

## 1. Overview

The Recommendation Engine is the core decision-support component of the **AI-Powered Enterprise Workload & Skill Orchestrator (EWSO)**.

Its purpose is to determine which employees are the most suitable candidates for a particular IT task by evaluating multiple factors rather than relying only on technical skill.

The engine considers:

* Technical skill match
* Skill proficiency
* Relevant experience
* Current workload
* Availability
* Task complexity
* Task priority
* Deadline
* Historical performance
* Learning goals
* Growth opportunities
* Employee/project constraints

The engine returns a ranked list of candidates together with an explanation of why each candidate received their score.

---

# 2. Design Philosophy

The recommendation engine is **decision support**, not autonomous task assignment.

```text
AI/Rules Engine
      ↓
Recommendations
      ↓
Manager
      ↓
Final Assignment
```

The manager remains responsible for the final decision.

This is important because real enterprise environments contain information that may not exist in the system, such as:

* undocumented domain knowledge
* customer relationships
* temporary team responsibilities
* production support obligations
* upcoming organizational changes
* manager-specific knowledge

Therefore:

> The system recommends. The manager decides.

---

# 3. Recommendation Flow

```text
Task Created
     ↓
Task Analysis
     ↓
Identify Required Skills
     ↓
Identify Task Complexity
     ↓
Identify Task Priority
     ↓
Fetch Eligible Employees
     ↓
Apply Hard Constraints
     ↓
Calculate Candidate Factors
     ↓
Calculate Weighted Score
     ↓
Apply Risk / Workload Rules
     ↓
Calculate Confidence
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

# 4. Example

Suppose a manager creates:

```text
Task:
Fix payment service timeout

Priority:
HIGH

Complexity:
COMPLEX

Estimated effort:
6 hours

Deadline:
10 September 2026

Required skills:
Java
Spring Boot
Microservices
Payment Systems
AWS
```

The system evaluates employees:

| Employee   | Skill | Experience | Workload | Availability | Final Score |
| ---------- | ----: | ---------: | -------: | -----------: | ----------: |
| Employee A |    95 |         90 |       80 |          100 |        91.2 |
| Employee B |    88 |         85 |       90 |          100 |        87.6 |
| Employee C |    92 |         70 |       45 |           80 |        82.4 |
| Employee D |    75 |         80 |       95 |          100 |        76.3 |

The engine returns:

```text
1. Employee A → 91.2
2. Employee B → 87.6
3. Employee C → 82.4
```

Instead of simply saying:

```text
Employee A is recommended.
```

the system explains:

```text
Employee A is recommended because:

+ Strong Java and Spring Boot proficiency
+ 3 years of relevant payment experience
+ Current workload is 60%
+ Available before the deadline

Negative factor:
- Limited AWS experience
```

---

# 5. Recommendation Architecture

```text
                 Recommendation Service
                          │
          ┌───────────────┼────────────────┐
          ↓               ↓                ↓
    Eligibility       Factor          Explanation
      Engine          Calculator         Engine
          │               │                │
          └───────────────┼────────────────┘
                          ↓
                    Score Engine
                          ↓
                    Ranking Engine
                          ↓
                  Confidence Engine
                          ↓
                 Recommendation Store
```

---

# 6. Hard Constraints vs Soft Factors

The engine must distinguish between **hard constraints** and **soft scoring factors**.

## Hard Constraints

A candidate can be excluded completely if they violate a mandatory rule.

Examples:

```text
Employee is inactive
Employee is unavailable
Employee is on leave
Mandatory skill is completely missing
Required minimum proficiency is not satisfied
Employee cannot complete the task before deadline
```

These conditions are evaluated before scoring.

---

## Soft Factors

Candidates who pass eligibility are scored using:

```text
Skill Match
Experience
Workload
Availability
Complexity Match
Performance
Learning Opportunity
```

This prevents the system from giving a high score to someone who fundamentally cannot perform the task.

---

# 7. Eligibility Engine

The eligibility engine performs the first filtering stage.

```text
Candidate
   ↓
Active?
   ↓
Available?
   ↓
Mandatory skills satisfied?
   ↓
Minimum proficiency satisfied?
   ↓
Deadline feasible?
   ↓
Eligible
```

---

# 8. Eligibility Rules

### Rule 1 — Employee must be active

```text
employmentStatus = ACTIVE
```

Inactive employees cannot be recommended.

---

### Rule 2 — Employee must be available

Possible states:

```text
AVAILABLE
PARTIALLY_AVAILABLE
BUSY
OVERLOADED
ON_LEAVE
UNAVAILABLE
```

Employees on leave are excluded.

---

### Rule 3 — Mandatory skills

Suppose a task requires:

```text
Java → mandatory
Spring Boot → mandatory
AWS → important
Kafka → optional
```

A candidate must satisfy:

```text
Java
Spring Boot
```

before being considered.

---

### Rule 4 — Minimum proficiency

Proficiency scale:

```text
1 → Beginner
2 → Basic
3 → Intermediate
4 → Advanced
5 → Expert
```

Example:

```text
Required Spring Boot proficiency = 4

Employee proficiency = 5
→ Eligible

Employee proficiency = 4
→ Eligible

Employee proficiency = 3
→ Not eligible for mandatory requirement
```

---

# 9. Overload Rule

Workload percentage:

```text
Workload % =
Assigned Hours / Available Capacity Hours × 100
```

Example:

```text
Assigned hours = 24
Capacity = 40

Workload = 24 / 40 × 100
         = 60%
```

Workload states:

| Workload | Status        |
| -------: | ------------- |
|    0–40% | AVAILABLE     |
|   41–70% | NORMAL        |
|   71–85% | BUSY          |
|  86–100% | OVERLOADED    |
|    >100% | OVER_CAPACITY |

An employee above 100% workload should generally be excluded from non-critical tasks.

A manager can override this decision.

---

# 10. Recommendation Score

Eligible candidates receive a score from 0 to 100.

The baseline scoring model is:

```text
Final Score =
    Skill Match × 0.30
  + Experience × 0.20
  + Workload × 0.20
  + Complexity Match × 0.10
  + Availability × 0.10
  + Performance × 0.05
  + Learning Opportunity × 0.05
```

Total:

```text
30 + 20 + 20 + 10 + 10 + 5 + 5 = 100%
```

---

# 11. Factor 1 — Skill Match

Weight:

```text
30%
```

Skill matching is the most important factor.

The engine compares:

```text
Required Skills
        vs
Employee Skills
```

Example:

```text
Task requires:

Java → 4
Spring Boot → 4
AWS → 3

Employee:

Java → 5
Spring Boot → 4
AWS → 3
```

The candidate receives a high skill-match score.

---

# 12. Multiple Skill Matching

The engine must not evaluate only the strongest skill.

Example:

```text
Task:
Java + Spring Boot + Kafka + AWS
```

Candidate A:

```text
Java → 5
Spring Boot → 5
Kafka → 1
AWS → 1
```

Candidate B:

```text
Java → 4
Spring Boot → 4
Kafka → 4
AWS → 4
```

Candidate B may be the better overall candidate even though Candidate A has stronger Java/Spring skills.

Therefore:

> Skill match must evaluate the complete required skill set.

---

# 13. Skill Match Calculation

For each required skill:

```text
Skill Score =
Employee Proficiency / Required Proficiency × 100
```

The engine caps the result at 100 for scoring purposes.

Example:

```text
Required = 4
Employee = 5

5 / 4 × 100 = 125
Capped = 100
```

For multiple skills:

```text
Overall Skill Match =
Weighted Average of Individual Skill Scores
```

Mandatory skills receive higher importance than optional skills.

---

# 14. Factor 2 — Relevant Experience

Weight:

```text
20%
```

Experience should be relevant to the task rather than simply total years of employment.

Example:

```text
Employee A:
5 years total experience
1 year payment experience

Employee B:
3 years total experience
2.5 years payment experience
```

For a payment-system task, Employee B may receive the higher relevant-experience score.

Relevant experience can be derived from:

* project history
* task history
* skill experience
* domain experience

---

# 15. Factor 3 — Workload

Weight:

```text
20%
```

The system prefers employees with reasonable workload.

Example:

```text
Employee A → 35%
Employee B → 65%
Employee C → 92%
```

Employee A generally receives the strongest workload score.

However:

```text
Low workload ≠ automatically best candidate
```

Skill and delivery feasibility remain important.

---

# 16. Workload Scoring

A baseline workload score can be:

```text
Workload Score = max(0, 100 - Workload Percentage)
```

Example:

```text
Workload = 30%

Score = 70
```

For overloaded employees, additional penalties can be applied.

The exact formula should remain configurable rather than hard-coded throughout the application.

---

# 17. Factor 4 — Task Complexity Match

Weight:

```text
10%
```

Task complexity:

```text
SIMPLE
MEDIUM
COMPLEX
EXPERT
```

The engine compares task complexity against:

* employee experience
* proficiency
* previous similar tasks
* performance history

Example:

```text
EXPERT task
+
Beginner employee
=
Low complexity-match score
```

An employee should not be assigned an expert production task simply because they have one related skill.

---

# 18. Factor 5 — Availability

Weight:

```text
10%
```

Availability considers:

* current availability
* available hours
* upcoming leave
* task deadline
* existing commitments

Example:

```text
Task effort = 8 hours
Deadline = tomorrow

Employee A:
Available hours before deadline = 12
→ Strong availability

Employee B:
Available hours before deadline = 3
→ Poor availability
```

---

# 19. Factor 6 — Past Performance

Weight:

```text
5%
```

Performance signals can include:

```text
On-time completion
Average completion time
Quality score
Reopened tasks
Incident resolution
Manager feedback
```

Example:

```text
On-time completion = 95%
Quality = 92%
Reopen rate = 3%
```

This contributes to the performance score.

---

# 20. Missing Performance Data

New employees may have limited history.

The system must not interpret:

```text
No performance data
```

as:

```text
Poor performance
```

Instead:

```text
Missing data → neutral contribution
```

The system should also reduce confidence when important information is unavailable.

---

# 21. Factor 7 — Learning Opportunity

Weight:

```text
5%
```

This is one of the differentiating features of EWSO.

Suppose:

```text
Employee wants to learn AWS.

Employee AWS proficiency = 3.

Task requires AWS = 4.

Employee has sufficient baseline skills.

Workload = 45%.

Task is MEDIUM priority.
```

The engine may increase the learning-opportunity score.

This creates:

```text
Business delivery
+
Employee development
```

---

# 22. Growth-Aware Allocation

The engine should support two modes.

### Delivery-first mode

```text
Critical production task
        ↓
Delivery capability
        ↓
Skill match
        ↓
Experience
        ↓
Workload
        ↓
Learning opportunity
```

### Growth-aware mode

For suitable non-critical tasks:

```text
Skill match
+
Workload
+
Experience
+
Learning Goal
```

can be used to identify development opportunities.

---

# 23. Critical Task Protection

Learning must never override critical delivery requirements.

Example:

```text
Task priority = CRITICAL

Employee:
AWS skill = 2
Learning goal = AWS
```

The system must **not** recommend the employee merely because AWS is their learning goal.

Instead:

```text
Experienced AWS employee
        ↓
Recommended
```

The learning employee can potentially be added as:

```text
Learning Opportunity
```

only if the business rules allow it and delivery risk remains acceptable.

---

# 24. Score Calculation Example

Suppose Employee A receives:

```text
Skill Match       = 95
Experience        = 90
Workload          = 80
Complexity        = 90
Availability      = 100
Performance       = 85
Learning          = 70
```

Then:

```text
95 × 0.30 = 28.50
90 × 0.20 = 18.00
80 × 0.20 = 16.00
90 × 0.10 =  9.00
100 × 0.10 = 10.00
85 × 0.05 =  4.25
70 × 0.05 =  3.50
```

Final:

```text
28.50
+18.00
+16.00
+ 9.00
+10.00
+ 4.25
+ 3.50
------
89.25
```

Final recommendation score:

```text
89.25 / 100
```

---

# 25. Score Normalization

All factor scores must be normalized to:

```text
0–100
```

before applying weights.

This prevents one factor from dominating because of a different numerical scale.

For example:

```text
Experience → 0–100
Workload → 0–100
Skill → 0–100
Performance → 0–100
```

---

# 26. Recommendation Threshold

The system can define a minimum recommendation threshold.

Example:

```text
Minimum recommendation score = 60
```

Candidates below the threshold may not appear in the primary recommendations.

However, the threshold should be configurable.

Example configuration:

```yaml
recommendation:
  minimum-score: 60
  top-candidates: 5
```

---

# 27. Candidate Ranking

After scoring:

```text
Candidate A → 91.2
Candidate B → 87.6
Candidate C → 82.4
Candidate D → 71.8
```

The ranking engine sorts candidates:

```text
1 → A
2 → B
3 → C
4 → D
```

The system should normally return the top 3–5 candidates rather than only one.

---

# 28. Tie-Breakers

If candidates have nearly identical scores, use:

```text
1. Higher mandatory skill match
2. Lower workload
3. Higher relevant experience
4. Higher performance
5. Earlier availability
```

Example:

```text
Employee A → 88.20
Employee B → 88.20
```

If:

```text
A skill match = 95
B skill match = 91
```

Employee A ranks higher.

---

# 29. Recommendation Explanation

Every recommendation should contain an explanation.

Example:

```json
{
  "employeeId": 101,
  "score": 91.2,
  "explanation": {
    "positiveFactors": [
      "Strong Spring Boot proficiency",
      "Relevant payment-domain experience",
      "Current workload is 60%",
      "Available before deadline"
    ],
    "negativeFactors": [
      "Limited AWS experience"
    ]
  }
}
```

The explanation should be generated from actual scoring factors.

It should never invent reasons.

---

# 30. Confidence Score

The recommendation should contain a confidence value.

Example:

```text
Confidence = 0.91
```

This represents confidence in the quality/completeness of the recommendation inputs, not a guarantee that the employee will successfully complete the task.

---

# 31. Confidence Inputs

Confidence can consider:

```text
Skill data completeness
Performance history availability
Workload data freshness
Task requirement clarity
Employee project history
Availability information
```

Example:

### High confidence

```text
All required skills known
Recent workload data
Strong performance history
Clear task requirements
Relevant project history
```

### Low confidence

```text
Few employee skills recorded
No performance history
Unclear task description
Stale workload data
Unknown availability
```

---

# 32. Missing Data Handling

The engine must distinguish:

```text
Missing information
```

from:

```text
Employee has zero capability
```

Example:

```text
AWS skill not recorded
```

does not automatically mean:

```text
AWS proficiency = 0
```

Instead:

```text
AWS proficiency = UNKNOWN
```

The recommendation explanation may say:

```text
AWS capability could not be verified from available employee data.
```

This is important for preventing unfair recommendations.

---

# 33. Data Freshness

Recommendation quality depends on fresh data.

Important freshness indicators:

```text
Employee workload
Employee availability
Employee skills
Performance
Task status
```

Example:

```text
Workload data updated:
09:55

Recommendation generated:
10:00
```

This is reliable.

But:

```text
Workload data updated:
3 days ago
```

should reduce confidence.

---

# 34. Recommendation Lifecycle

```text
GENERATED
    ↓
PENDING_REVIEW
    ↓
    ├── ACCEPTED
    │
    ├── REJECTED
    │
    └── OVERRIDDEN
```

Possible expiration:

```text
EXPIRED
```

A recommendation may become invalid if:

```text
Employee becomes unavailable
Task requirements change
Task is reassigned
Workload changes significantly
Deadline changes
```

---

# 35. Manager Override

Managers can select another employee.

Example:

```text
AI Recommendation:

Employee A → 91
Employee B → 87
```

Manager selects:

```text
Employee C → 76
```

The system records:

```text
recommendedEmployee = A
selectedEmployee = C
overrideReason = "Existing customer knowledge"
```

This should be stored for audit and future analytics.

---

# 36. Recommendation History

Every recommendation should be persisted.

Example:

```text
Recommendation
------------------------
ID
Task ID
Employee ID
Score
Confidence
Rank
Generated At
Decision
Decision By
Decision At
Override Reason
```

This enables future analysis such as:

```text
How often do managers accept recommendations?
```

```text
Which recommendations are frequently overridden?
```

```text
Why are recommendations being overridden?
```

---

# 37. Feedback Loop

Manager decisions can become valuable feedback.

Example:

```text
AI recommends Employee A

Manager chooses Employee B

Reason:
Employee B has customer-specific knowledge
```

This can reveal:

```text
Missing skill/domain information
```

Future versions can use this information to improve the system.

The initial implementation should store the feedback rather than automatically retraining a model.

---

# 38. Recommendation Service Components

Recommended package structure:

```text
recommendation-service/
└── src/main/java/com/ewso/recommendation/
    ├── controller/
    │   └── RecommendationController.java
    │
    ├── service/
    │   ├── RecommendationService.java
    │   ├── EligibilityService.java
    │   ├── ScoringService.java
    │   ├── RankingService.java
    │   ├── ConfidenceService.java
    │   └── ExplanationService.java
    │
    ├── domain/
    │   ├── Recommendation.java
    │   ├── RecommendationFactor.java
    │   └── CandidateScore.java
    │
    ├── strategy/
    │   ├── SkillScoringStrategy.java
    │   ├── ExperienceScoringStrategy.java
    │   ├── WorkloadScoringStrategy.java
    │   ├── AvailabilityScoringStrategy.java
    │   ├── PerformanceScoringStrategy.java
    │   └── LearningOpportunityStrategy.java
    │
    ├── client/
    │   ├── EmployeeClient.java
    │   ├── SkillClient.java
    │   ├── WorkloadClient.java
    │   ├── TaskClient.java
    │   └── PerformanceClient.java
    │
    ├── repository/
    │   └── RecommendationRepository.java
    │
    ├── dto/
    │   ├── RecommendationRequest.java
    │   ├── RecommendationResponse.java
    │   └── RecommendationFactorResponse.java
    │
    ├── event/
    │   ├── RecommendationGeneratedEvent.java
    │   └── RecommendationDecisionEvent.java
    │
    └── config/
        └── RecommendationProperties.java
```

---

# 39. Strategy Pattern

Scoring should not become one huge method.

Avoid:

```java
calculateEverything()
```

Instead:

```text
SkillScoringStrategy
ExperienceScoringStrategy
WorkloadScoringStrategy
AvailabilityScoringStrategy
PerformanceScoringStrategy
LearningOpportunityStrategy
```

Each strategy calculates one factor.

This makes the system:

* testable
* configurable
* maintainable
* extensible

---

# 40. Example Strategy Interface

```java
public interface ScoringStrategy {

    ScoreResult calculate(
        EmployeeContext employee,
        TaskContext task
    );

}
```

Example:

```java
public class SkillScoringStrategy
        implements ScoringStrategy {

    @Override
    public ScoreResult calculate(
            EmployeeContext employee,
            TaskContext task) {

        // Calculate skill match

        return ScoreResult.builder()
                .factor("SKILL_MATCH")
                .score(score)
                .build();
    }
}
```

---

# 41. Weighted Score Service

The scoring service combines factor results.

Conceptually:

```java
finalScore =
      skillScore * skillWeight
    + experienceScore * experienceWeight
    + workloadScore * workloadWeight
    + complexityScore * complexityWeight
    + availabilityScore * availabilityWeight
    + performanceScore * performanceWeight
    + learningScore * learningWeight;
```

Weights should come from configuration.

---

# 42. Configurable Weights

Example:

```yaml
recommendation:
  weights:
    skill-match: 0.30
    experience: 0.20
    workload: 0.20
    complexity: 0.10
    availability: 0.10
    performance: 0.05
    learning-opportunity: 0.05
```

The system should validate:

```text
Sum of weights = 1.00
```

If not:

```text
Application startup should fail
```

or configuration should be rejected.

---

# 43. Recommendation API

Main endpoint:

```http
POST /api/v1/recommendations/tasks/{taskId}
```

Response:

```json
{
  "taskId": "TASK-1024",
  "recommendations": [
    {
      "recommendationId": 9001,
      "employeeId": 101,
      "rank": 1,
      "score": 91.2,
      "confidence": 0.91,
      "status": "RECOMMENDED"
    },
    {
      "recommendationId": 9002,
      "employeeId": 107,
      "rank": 2,
      "score": 84.7,
      "confidence": 0.87,
      "status": "RECOMMENDED"
    }
  ]
}
```

---

# 44. Recommendation Processing

Pseudo-code:

```text
function recommend(task):

    taskContext = loadTask(task)

    requirements = analyzeRequirements(taskContext)

    employees = findCandidateEmployees()

    eligibleCandidates = []

    for employee in employees:

        if not isActive(employee):
            continue

        if not isAvailable(employee):
            continue

        if not satisfiesMandatorySkills(employee, requirements):
            continue

        if not satisfiesMinimumProficiency(employee, requirements):
            continue

        if not deadlineFeasible(employee, task):
            continue

        eligibleCandidates.add(employee)

    scoredCandidates = []

    for employee in eligibleCandidates:

        skillScore =
            calculateSkillMatch(employee, task)

        experienceScore =
            calculateExperience(employee, task)

        workloadScore =
            calculateWorkload(employee)

        complexityScore =
            calculateComplexityMatch(employee, task)

        availabilityScore =
            calculateAvailability(employee, task)

        performanceScore =
            calculatePerformance(employee)

        learningScore =
            calculateLearningOpportunity(employee, task)

        finalScore =
            weightedScore(
                skillScore,
                experienceScore,
                workloadScore,
                complexityScore,
                availabilityScore,
                performanceScore,
                learningScore
            )

        confidence =
            calculateConfidence(employee, task)

        scoredCandidates.add(
            CandidateScore(...)
        )

    rankedCandidates =
        rankCandidates(scoredCandidates)

    explanations =
        generateExplanations(rankedCandidates)

    saveRecommendations(rankedCandidates)

    publishRecommendationEvent()

    return rankedCandidates
```

---

# 45. Important Business Rule

The engine must not simply do:

```text
highest score = automatic assignment
```

Instead:

```text
highest score
       ↓
recommendation
       ↓
manager review
       ↓
assignment
```

This protects the system from over-automation.

---

# 46. AI Integration

The initial recommendation engine should be deterministic.

```text
Task
 ↓
Rules + Scoring
 ↓
Recommendation
```

AI/LLM capabilities can later enhance:

```text
Task classification
Skill extraction
Task summarization
Domain detection
Complexity estimation
Semantic similarity
Explanation generation
```

The LLM should not initially control the final assignment decision.

---

# 47. AI-Assisted Task Understanding

Example task:

```text
"Payment API is intermittently failing with timeout
while calling the external provider. Need to investigate
Spring Boot RestTemplate timeout configuration and retry
behavior."
```

AI can extract:

```json
{
  "skills": [
    "Java",
    "Spring Boot",
    "REST API",
    "Microservices",
    "Payment Systems"
  ],
  "complexity": "COMPLEX",
  "domain": "PAYMENTS",
  "keywords": [
    "timeout",
    "retry",
    "external provider"
  ]
}
```

The recommendation engine then uses these structured requirements.

---

# 48. AI Failure Handling

If the AI component fails:

```text
AI unavailable
      ↓
Use explicit task skills
      ↓
Use manager-provided complexity
      ↓
Use deterministic recommendation
```

The task management system must continue functioning.

Therefore:

> AI improves the system but is not a single point of failure.

---

# 49. Explainability Requirement

The system must answer:

```text
Why was Employee A recommended?
```

and:

```text
Why was Employee B ranked below Employee A?
```

Example:

```text
Employee A ranked #1 because:

Skill Match: 95
Experience: 90
Workload: 60%
Availability: High
Performance: 92

Employee B ranked #2 because:

Skill Match: 92
Experience: 80
Workload: 85%
Availability: High
```

This is more useful to managers than a black-box prediction.

---

# 50. Recommendation Quality Metrics

The system should track:

```text
Recommendation acceptance rate
Manager override rate
Average recommendation score
Top-1 selection rate
Top-3 selection rate
Task completion success
On-time completion
Recommendation-to-assignment time
```

Example:

```text
Recommendation acceptance rate:
78%

Top-3 selection rate:
91%

Manager override rate:
22%
```

These metrics can be used to evaluate the system.

---

# 51. Fairness Considerations

The engine should use job-relevant attributes.

Recommended inputs:

```text
Skills
Experience
Workload
Availability
Performance
Task history
Learning goals
```

Avoid using irrelevant personal attributes for task assignment.

The recommendation process should also be auditable.

---

# 52. Auditability

For every recommendation, store:

```text
Task
Candidate
Factor scores
Weights
Final score
Confidence
Timestamp
Decision
Manager
Override reason
```

This allows the organization to understand how a recommendation was generated.

---

# 53. Caching

Frequently accessed information can be cached using Redis.

Examples:

```text
employee:{id}:skills
employee:{id}:availability
employee:{id}:workload
employee:{id}:performance
recommendation:{taskId}
```

However, recommendation results must be invalidated when important underlying information changes.

---

# 54. Recommendation Cache Invalidation

Invalidate/recalculate when:

```text
Task requirements change
Task priority changes
Task deadline changes
Employee skill changes
Employee availability changes
Employee workload changes significantly
Employee performance changes
```

Example:

```text
employee.skill.updated
        ↓
Redis cache invalidation
        ↓
Existing recommendations marked stale
```

---

# 55. Kafka Events

Recommendation-related events:

```text
recommendation.generated
recommendation.accepted
recommendation.rejected
recommendation.overridden
recommendation.expired
```

Example:

```json
{
  "eventType": "recommendation.generated",
  "recommendationId": 9001,
  "taskId": "TASK-1024",
  "generatedAt": "2026-09-07T10:30:00Z"
}
```

---

# 56. Recommendation Event Flow

```text
Task Created
     ↓
Kafka: task.created
     ↓
Recommendation Service
     ↓
Generate Recommendation
     ↓
Kafka: recommendation.generated
     ↓
Analytics Service
```

The recommendation API can also trigger recommendation generation synchronously when a manager explicitly requests it.

---

# 57. Resilience

The Recommendation Service depends on multiple services.

```text
Recommendation
      │
      ├── Employee
      ├── Skill
      ├── Workload
      ├── Task
      └── Performance
```

Each dependency should have:

```text
Timeout
Retry
Circuit Breaker
Fallback
```

Example:

```text
Workload Service unavailable
        ↓
Use last known workload if sufficiently fresh
        ↓
Reduce confidence
        ↓
Continue recommendation
```

If reliable workload information is unavailable for a critical decision, the system may instead mark the recommendation as requiring manager review.

---

# 58. Performance Target

For a normal enterprise team size:

```text
Recommendation generation target:
< 2 seconds
```

Performance should be improved using:

```text
Redis
Efficient DB queries
Parallel data retrieval where appropriate
Pagination
Caching
Precomputed workload data
```

---

# 59. Testing Strategy

The recommendation engine requires extensive testing because it contains business-critical logic.

## Unit Tests

Test:

```text
Skill scoring
Experience scoring
Workload scoring
Availability scoring
Performance scoring
Learning scoring
Weighted calculation
Ranking
Tie-breakers
Confidence
Eligibility
```

---

## Business Rule Tests

Examples:

```text
Inactive employee → excluded

Employee on leave → excluded

Missing mandatory skill → excluded

Insufficient mandatory proficiency → excluded

Overloaded employee → penalized/excluded according to rules

Critical task → learning opportunity cannot override delivery

Missing performance data → neutral score

Missing skill data → unknown, not zero
```

---

# 60. Example Unit Test

```java
@Test
void shouldExcludeEmployeeWithoutMandatorySkill() {

    Employee employee = employeeWithoutSkill("Spring Boot");

    Task task = taskRequiringMandatorySkill(
        "Spring Boot",
        4
    );

    boolean eligible =
        eligibilityService.isEligible(
            employee,
            task
        );

    assertFalse(eligible);
}
```

---

# 61. Integration Test

Test the complete flow:

```text
POST /recommendations/tasks/TASK-1024
       ↓
Recommendation Service
       ↓
Employee Service
       ↓
Skill Service
       ↓
Workload Service
       ↓
Performance Service
       ↓
Scoring
       ↓
Database
       ↓
Response
```

Testcontainers should be used for:

```text
MySQL
Redis
Kafka
```

where appropriate.

---

# 62. Example Recommendation Test Data

Employee A:

```text
Java = 5
Spring Boot = 5
AWS = 3
Experience = 4 years
Workload = 45%
Performance = 92
Availability = HIGH
```

Employee B:

```text
Java = 4
Spring Boot = 4
AWS = 4
Experience = 3 years
Workload = 75%
Performance = 88
Availability = HIGH
```

Task:

```text
Java = mandatory, 4
Spring Boot = mandatory, 4
AWS = important, 3

Complexity = COMPLEX
Priority = HIGH
Estimated effort = 6 hours
```

Expected:

```text
Employee A > Employee B
```

---

# 63. Future ML Recommendation Model

The deterministic engine can later become the baseline against which ML models are evaluated.

Future architecture:

```text
                    Recommendation System
                            │
              ┌─────────────┴──────────────┐
              ↓                            ↓
      Deterministic Engine            ML Model
              │                            │
              └─────────────┬──────────────┘
                            ↓
                     Decision Layer
                            ↓
                       Recommendation
```

The deterministic system remains important because it provides:

* explainability
* predictable behavior
* business-rule enforcement
* fallback
* baseline for evaluating ML

---

# 64. Future Machine Learning Inputs

Potential features:

```text
Employee skill vector
Task skill vector
Skill similarity
Historical task success
Task completion time
Workload
Deadline pressure
Project/domain similarity
Task complexity
Manager selection history
Learning goals
```

Potential future models:

```text
Learning-to-rank
Gradient boosting
Classification
Recommendation models
Semantic embeddings
```

These should be introduced only after sufficient historical data is available.

---

# 65. Future Semantic Matching

A future version may compare:

```text
Task description
        ↓
Embedding
        ↓
Employee project/task history
        ↓
Embeddings
        ↓
Semantic similarity
```

This allows the system to discover relationships such as:

```text
"OAuth payment timeout"

≈

"PayPal API authentication issue"
```

even when exact keywords differ.

---

# 66. Future Project Knowledge Integration

The Recommendation Engine can eventually use the Knowledge Service.

Example:

```text
Task:
Investigate payment-service timeout
```

Knowledge Service finds:

```text
Previous incident:
Payment timeout occurred in Project PAY-001.

Developer:
Employee 108

Resolution:
Configured provider timeout and circuit breaker.
```

The recommendation engine can then identify Employee 108 as having strong historical relevance.

This creates a powerful future capability:

```text
Current task
+
Employee skills
+
Employee workload
+
Historical project knowledge
=
Context-aware recommendation
```

---

# 67. Golden Rules

The Recommendation Engine must follow these rules:

```text
1. Never recommend inactive employees.

2. Never recommend employees on leave.

3. Mandatory skills must be satisfied.

4. Minimum proficiency requirements must be respected.

5. Workload must influence recommendations.

6. Critical tasks prioritize delivery over learning.

7. Missing data must not automatically mean zero capability.

8. Recommendation score must be explainable.

9. Confidence must reflect data quality.

10. Managers retain final authority.

11. Manager overrides must be recorded.

12. AI must not become a single point of failure.

13. Scoring weights must be configurable.

14. Recommendations must be auditable.

15. Core business rules belong in the backend domain/service layer.
```

---

# 68. Recommended Implementation Order

Build the engine incrementally.

### Phase 1 — Eligibility

Implement:

```text
Active employee
Availability
Mandatory skills
Minimum proficiency
Deadline feasibility
```

### Phase 2 — Basic Scoring

Implement:

```text
Skill Match
Experience
Workload
Availability
```

### Phase 3 — Advanced Scoring

Add:

```text
Complexity
Performance
Learning Opportunity
```

### Phase 4 — Explainability

Add:

```text
Factor breakdown
Positive factors
Negative factors
Ranking explanation
```

### Phase 5 — Confidence

Add:

```text
Data completeness
Data freshness
Task clarity
Historical evidence
```

### Phase 6 — Events and Caching

Add:

```text
Kafka
Redis
Recommendation invalidation
```

### Phase 7 — AI

Add:

```text
Task classification
Skill extraction
Complexity prediction
Semantic matching
```

### Phase 8 — Advanced Intelligence

Add:

```text
ML ranking
Project knowledge
RAG
Historical organizational knowledge
Predictive workforce planning
```

---

# 69. Definition of Done

The Recommendation Engine MVP is complete when:

```text
[ ] Candidate eligibility works
[ ] Mandatory skills are enforced
[ ] Proficiency is evaluated
[ ] Workload is calculated
[ ] Availability is evaluated
[ ] Experience is considered
[ ] Weighted score is calculated
[ ] Candidates are ranked
[ ] Top candidates are returned
[ ] Explanations are generated
[ ] Confidence is calculated
[ ] Recommendations are persisted
[ ] Manager can accept/reject
[ ] Manager can override
[ ] Overrides are audited
[ ] Redis caching works
[ ] Kafka events work
[ ] Failure handling works
[ ] Unit tests cover scoring rules
[ ] Integration tests cover recommendation flow
```

---

# 70. Final Architecture

```text
                         TASK
                           │
                           ↓
                 ┌─────────────────┐
                 │ Task Intelligence│
                 └────────┬────────┘
                          ↓
                  Required Skills
                  Complexity
                  Priority
                  Deadline
                          │
                          ↓
                ┌───────────────────┐
                │ Eligibility Engine│
                └─────────┬─────────┘
                          │
                   Eligible Users
                          │
          ┌───────────────┼────────────────┐
          ↓               ↓                ↓
      Skill Data      Workload Data    Performance
          │               │                │
          └───────────────┼────────────────┘
                          ↓
                 ┌────────────────┐
                 │ Scoring Engine │
                 └───────┬────────┘
                         ↓
                 Weighted Score
                         ↓
                 ┌───────────────┐
                 │ Ranking Engine│
                 └───────┬───────┘
                         ↓
                Confidence Engine
                         ↓
               Explanation Engine
                         ↓
              Recommendation Store
                         ↓
                 Manager Dashboard
                         │
              ┌──────────┴──────────┐
              ↓                     ↓
           Accept                Override
              │                     │
              └──────────┬──────────┘
                         ↓
                   Task Assignment
                         ↓
                       Kafka
                  /      │       \
                 ↓       ↓        ↓
             Workload Notification Analytics
```

---

## Core Principle

The most important design principle of the EWSO Recommendation Engine is:

> **The best employee is not necessarily the employee with the highest skill. The best candidate is the employee who can successfully deliver the task while considering skills, experience, workload, availability, performance, and—when safe—future growth.**

This makes the engine an **enterprise workforce decision-support system**, rather than a simple employee-to-task matching algorithm.
