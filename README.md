# Insurance Quote Service

A production-quality Proof of Concept for an Insurance Quote System built with **Spring Boot 4.x**, **Temporal** workflow orchestration, **GoRules** business rules engine, and **PostgreSQL**.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API Layer (REST)                             │
│                   QuoteController (POST/GET)                        │
└──────────────┬──────────────────────────────────────┬───────────────┘
               │                                      │
               ▼                                      ▼
┌──────────────────────┐              ┌──────────────────────────────┐
│   Temporal Workflow   │              │     Business Service         │
│  (Orchestration)     │              │  InsuranceQuoteService       │
│                      │              │  (Persistence + Logic)       │
│  ┌────────────────┐  │              └──────────────────────────────┘
│  │ ValidateActivity│  │                          │
│  └───────┬────────┘  │                          ▼
│  ┌───────▼────────┐  │              ┌──────────────────────────────┐
│  │DecisionActivity│  │              │     InsuranceQuoteMapper     │
│  └───────┬────────┘  │              └──────────────────────────────┘
│  ┌───────▼────────┐  │                          │
│  │PersistActivity │──┼──────────────────────────▶│
│  └───────┬────────┘  │              ┌───────────▼──────────────────┐
│  ┌───────▼────────┐  │              │   InsuranceQuoteRepository   │
│  │NotificationAct │  │              │      (JPA / PostgreSQL)      │
│  └────────────────┘  │              └──────────────────────────────┘
└──────────────────────┘
               │
               ▼
┌──────────────────────┐
│   GoRules Engine     │
│   (Business Rules)   │
│                      │
│  eligibility.json    │
│  pricing.json        │
│  discount.json       │
│  (Hot Reload)        │
└──────────────────────┘
```

## Sequence Diagram

```
Client          Controller       Temporal         Activities        GoRules         Database
  │                │                │                │                │                │
  │──POST /quotes─▶│                │                │                │                │
  │                │──startWorkflow▶│                │                │                │
  │                │                │──validate()───▶│                │                │
  │                │                │                │──evaluate()───▶│                │
  │                │                │◀──result───────│◀──result──────│                │
  │                │                │──evaluateRisk()▶│               │                │
  │                │                │                │──evaluate()───▶│                │
  │                │                │◀──result───────│◀──result──────│                │
  │                │                │──saveQuote()───▶│               │                │
  │                │                │                │────save()──────────────────────▶│
  │                │                │                │◀───────────────────────────────│
  │                │                │◀──result───────│                │                │
  │                │                │──sendNotif()──▶│                │                │
  │                │◀──response────│◀──result───────│                │                │
  │◀──201 Created──│                │                │                │                │
```

## Temporal Workflow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    InsuranceQuoteWorkflow                        │
│                                                                 │
│  ┌──────────┐    ┌──────────────┐    ┌────────────┐    ┌──────┐│
│  │ Validate  │───▶│Decision(Go) │───▶│  Persist   │───▶│Notify││
│  │ Activity  │    │  Activity   │    │  Activity  │    │ Act. ││
│  └────┬─────┘    └──────┬───────┘    └─────┬──────┘    └──────┘│
│       │                 │                  │                    │
│       ▼                 ▼                  ▼                    │
│   Validation       Risk/Premium        Save to DB          Send Email
│   Check            Calculation         Insurance           Notification
│                                     Quote
└─────────────────────────────────────────────────────────────────┘
```

## GoRules Decision Flow

```
┌─────────────────┐
│   Quote Input    │
│ (age, vehicle,   │
│  claims, tier)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌─────────────────┐
│  eligibility.json│────▶│  REJECTED if    │
│  (Age >= 18?)    │     │  age < 18 or    │
└────────┬────────┘     │  income < 30k   │
         │ pass          └─────────────────┘
         ▼
┌─────────────────┐
│   pricing.json   │
│  (Risk Score)    │
│  age × vehicle   │
│  × claims        │
└────────┬────────┘
         │ riskScore, riskCategory, baseMultiplier
         ▼
┌─────────────────┐
│  discount.json   │
│  (Loyalty/       │
│   Claim-free)    │
└────────┬────────┘
         │ discountPercentage
         ▼
┌─────────────────┐
│ Final Premium =  │
│ base × multiplier│
│ × (1 - discount) │
└─────────────────┘
```

## Project Structure

```
insurance-quote-service/
├── docker/
│   └── postgres/
│       └── init-databases.sql
├── docker/temporal/config/dynamicconfig/
│   └── development-sql.yaml
├── docs/
│   └── GORULES.md
├── src/
│   ├── main/
│   │   ├── java/com/example/insurance/
│   │   │   ├── InsuranceQuoteServiceApplication.java
│   │   │   ├── activity/
│   │   │   │   ├── DecisionActivity.java
│   │   │   │   ├── DecisionActivityImpl.java
│   │   │   │   ├── NotificationActivity.java
│   │   │   │   ├── NotificationActivityImpl.java
│   │   │   │   ├── PersistActivity.java
│   │   │   │   ├── PersistActivityImpl.java
│   │   │   │   ├── ValidateActivity.java
│   │   │   │   └── ValidateActivityImpl.java
│   │   │   ├── config/
│   │   │   │   ├── HealthConfig.java
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── TemporalConfig.java
│   │   │   │   └── WorkerConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── BaseController.java
│   │   │   │   └── QuoteController.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── DecisionResult.java
│   │   │   │   ├── NotificationResult.java
│   │   │   │   ├── QuoteRequest.java
│   │   │   │   ├── QuoteResponse.java
│   │   │   │   └── ValidationResult.java
│   │   │   ├── entity/
│   │   │   │   ├── BaseEntity.java
│   │   │   │   └── InsuranceQuote.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InsuranceException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── mapper/
│   │   │   │   ├── BaseMapper.java
│   │   │   │   └── InsuranceQuoteMapper.java
│   │   │   ├── repository/
│   │   │   │   ├── BaseRepository.java
│   │   │   │   └── InsuranceQuoteRepository.java
│   │   │   ├── rules/
│   │   │   │   ├── GoRulesEngine.java
│   │   │   │   ├── GoRulesEngineImpl.java
│   │   │   │   └── RuleManager.java
│   │   │   ├── service/
│   │   │   │   ├── InsuranceQuoteService.java
│   │   │   │   └── InsuranceQuoteServiceImpl.java
│   │   │   └── workflow/
│   │   │       ├── InsuranceQuoteWorkflow.java
│   │   │       └── InsuranceQuoteWorkflowImpl.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       ├── application-dev.yml
│   │       ├── logback-spring.xml
│   │       └── rules/
│   │           ├── eligibility.json
│   │           ├── pricing.json
│   │           └── discount.json
│   └── test/
│       ├── java/com/example/insurance/
│       │   ├── InsuranceQuoteServiceApplicationTests.java
│       │   ├── controller/QuoteControllerTest.java
│       │   ├── repository/InsuranceQuoteRepositoryTest.java
│       │   └── service/InsuranceQuoteServiceTest.java
│       └── resources/
│           └── application-test.yml
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Docker Setup

### docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:18.4
    container_name: insurance-postgres
    environment:
      POSTGRES_DB: insurance_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./docker/postgres/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  temporal:
    image: temporalio/auto-setup:1.26.3
    container_name: insurance-temporal
    ports:
      - "7233:7233"
      - "8081:8080"
    environment:
      - DB=postgresql
      - DB_PORT=5432
      - POSTGRES_USER=postgres
      - POSTGRES_PWD=postgres
      - POSTGRES_SEEDS=postgres
    depends_on:
      postgres:
        condition: service_healthy

  temporal-ui:
    image: temporalio/ui:2.31.0
    container_name: insurance-temporal-ui
    ports:
      - "8082:8080"
    environment:
      - TEMPORAL_ADDRESS=temporal:7233
    depends_on:
      - temporal

  app:
    build: .
    container_name: insurance-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - TEMPORAL_CONNECTION_TARGET=temporal:7233
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/insurance_db?stringtype=unspecified
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    depends_on:
      postgres:
        condition: service_healthy
      temporal:
        condition: service_started

volumes:
  postgres-data:
```

## Database Setup

```sql
-- docker/postgres/init-databases.sql
CREATE DATABASE insurance_db;
\c insurance_db;
CREATE SCHEMA IF NOT EXISTS insurance;
```

## Running Instructions

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Option 1: Docker (Recommended)

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

### Option 2: Local Development

```bash
# 1. Start PostgreSQL and Temporal
docker-compose up -d postgres temporal temporal-ui

# 2. Run the application
mvn spring-boot:run

# 3. Access endpoints
# API:        http://localhost:8080/api/v1/quotes
# Swagger:    http://localhost:8080/api/swagger-ui.html
# Actuator:   http://localhost:8080/api/actuator/health
# Temporal:   http://localhost:8082
```

## Sample GoRules JSON

### eligibility.json
```json
{
  "nodes": [
    { "id": "input", "type": "inputNode", "name": "Input" },
    {
      "id": "eligibility-table",
      "type": "decisionTableNode",
      "name": "Eligibility Rules",
      "content": {
        "hitPolicy": "first",
        "inputs": [
          { "id": "i1", "name": "Age", "field": "age" },
          { "id": "i2", "name": "Annual Income", "field": "income" },
          { "id": "i3", "name": "Coverage Type", "field": "coverageType" }
        ],
        "outputs": [
          { "id": "o1", "name": "Eligible", "field": "eligible" },
          { "id": "o2", "name": "Status", "field": "status" },
          { "id": "o3", "name": "Reason", "field": "reason" }
        ],
        "rules": [
          { "_id": "r1", "i1": "< 18", "i2": "", "i3": "", "o1": "false", "o2": "\"REJECTED\"", "o3": "\"Age must be 18 or older\"" },
          { "_id": "r2", "i1": ">= 18", "i2": "", "i3": "", "o1": "true", "o2": "\"APPROVED\"", "o3": "\"Eligible\"" }
        ]
      }
    },
    { "id": "output", "type": "outputNode", "name": "Output" }
  ],
  "edges": [
    { "id": "e1", "sourceId": "input", "targetId": "eligibility-table", "type": "edge" },
    { "id": "e2", "sourceId": "eligibility-table", "targetId": "output", "type": "edge" }
  ]
}
```

## Sample Requests

### Create Quote
```bash
curl -X POST http://localhost:8080/api/v1/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "age": 30,
    "vehicleType": "SEDAN",
    "vehicleValue": 35000,
    "claimHistory": 0
  }'
```

### Get Quote
```bash
curl http://localhost:8080/api/v1/quotes/{id}
```

## Sample Responses

### Create Quote (201 Created)
```json
{
  "success": true,
  "message": "Quote processed successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "workflowId": "insurance-quote-workflow-abc123",
    "customerName": "John Doe",
    "age": 30,
    "vehicleType": "SEDAN",
    "vehicleValue": 35000,
    "claimHistory": 0,
    "premiumAmount": 720.00,
    "discount": 0.25,
    "riskCategory": "LOW_RISK",
    "status": "APPROVED",
    "createdDate": "2026-07-15T10:30:00"
  }
}
```

### Get Quote (200 OK)
```json
{
  "success": true,
  "message": "Quote retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "workflowId": "insurance-quote-workflow-abc123",
    "customerName": "John Doe",
    "age": 30,
    "vehicleType": "SEDAN",
    "vehicleValue": 35000,
    "claimHistory": 0,
    "premiumAmount": 720.00,
    "discount": 0.25,
    "riskCategory": "LOW_RISK",
    "status": "APPROVED",
    "createdDate": "2026-07-15T10:30:00"
  }
}
```

## Why Temporal?

| Aspect | Benefit |
|--------|---------|
| **Durability** | Workflows survive process crashes. If the app restarts mid-quote, Temporal resumes from the last completed step. |
| **Retries** | Automatic retry with exponential backoff for failed activities (e.g., DB timeout). |
| **Visibility** | Temporal UI shows every workflow execution, input, output, and timing. |
| **Scalability** | Workers can be scaled independently. Add more workers for higher throughput. |
| **Code as Workflow** | Business logic is written in plain Java. No DSL or visual builders needed. |
| **Compensation** | Built-in support for Saga patterns if rollback is needed. |

## Why GoRules?

| Aspect | Benefit |
|--------|---------|
| **No Redeploy** | Rules are JSON files. Modify and hot-reload without restarting the app. |
| **Business User Friendly** | Rules are authored in a visual editor (GoRules BRMS). Developers don't write conditions. |
| **Version Control** | Rules are JSON files stored in Git. Full audit trail of changes. |
| **Decision Tables** | Spreadsheet-like format that business analysts understand. |
| **Expression Language** | Built-in ZEN expressions for complex calculations. |
| **Multiple SDKs** | Same rules work across Java, Python, Go, Rust, .NET. |

## Why Separate Database Schemas?

| Schema | Purpose |
|--------|---------|
| `insurance` | Application data: quotes, policies, claims |
| `temporal` | Temporal server state: workflows, activities, history |
| `temporal_visibility` | Temporal visibility: workflow queries, search |

**Benefits:**
- **Isolation**: Temporal internal tables don't pollute business schema
- **Backup**: Business data and workflow data can be backed up independently
- **Security**: Separate access controls for Temporal vs application data
- **Performance**: Different indexing strategies for different query patterns

## How Rule Hot Reload Works

```
┌─────────────┐    File Change    ┌─────────────┐    Reload    ┌─────────────┐
│  rules/*.json│──────────────────▶│ WatchService │────────────▶│ RuleManager │
│  (on disk)   │                   │ (Java NIO)   │             │ (in-memory) │
└─────────────┘                   └─────────────┘             └──────┬──────┘
                                                                     │
                                                              Update Cache
                                                                     │
                                                              ┌──────▼──────┐
                                                              │  GoRules    │
                                                              │  Engine     │
                                                              │  (next eval │
                                                              │   uses new  │
                                                              │   rules)    │
                                                              └─────────────┘
```

**Flow:**
1. `RuleManager` registers a `WatchService` on `src/main/resources/rules/`
2. When a `.json` file is modified/created/deleted, the watcher detects it
3. The modified rule is reloaded from disk
4. The decision cache is updated
5. Next API call uses the new rules automatically
6. **No restart required**

## How to Extend

### Policy Issuance

```java
// Add new activity
@ActivityInterface
public interface PolicyActivity {
    PolicyResult issuePolicy(QuoteResponse quote);
}

// Add new workflow step
PolicyResult policyResult = policyActivity.issuePolicy(persistedResponse);

// Add new decision table
policy.json - determines policy terms based on quote
```

### Claim Processing

```java
// New workflow
@WorkflowInterface
public interface ClaimWorkflow {
    ClaimResult processClaim(ClaimRequest request);
}

// Steps: validate → assess → approve/deny → pay
// New GoRules: claim-eligibility.json, claim-assessment.json
```

### Renewals

```java
// Scheduled workflow (Temporal cron)
WorkflowOptions options = WorkflowOptions.newBuilder()
    .setCronSchedule("0 0 1 * *") // Monthly
    .build();

// Steps: fetch expiring → recalculate → send offer → track
// GoRules: renewal-pricing.json (may differ from new business)
```

## SOLID Principles Applied

| Principle | Implementation |
|-----------|---------------|
| **S** - Single Responsibility | Each activity does one thing. `ValidateActivity` only validates. `DecisionActivity` only evaluates rules. |
| **O** - Open/Closed | New activities can be added without modifying existing ones. New GoRules decisions can be added without code changes. |
| **L** - Liskov Substitution | All activities implement their interfaces. Temporal can substitute any implementation. |
| **I** - Interface Segregation | Separate interfaces for each activity. No fat interfaces. |
| **D** - Dependency Inversion | `DecisionActivityImpl` depends on `GoRulesEngine` interface, not concrete implementation. |

## API Documentation

Access Swagger UI at: `http://localhost:8080/api/swagger-ui.html`

## Health Checks

```bash
# Application health
curl http://localhost:8080/api/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "temporal": { "status": "UP", "details": { "temporal": "Connected" } }
  }
}
```

## License

Apache 2.0
