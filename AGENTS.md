# AGENTS.md

## Stack

- Java 21, Spring Boot 4.1.0, Temporal SDK 1.36.1, GoRules Zen Engine 0.4.7
- PostgreSQL 18.4 (single instance, schema isolation: `insurance` for business, Temporal auto-creates `temporal` + `temporal_visibility`)
- Maven build, Lombok, MapStruct

## Commands

```bash
# Compile
mvn compile

# Run (requires Docker: postgres + temporal)
docker-compose up -d postgres temporal
mvn spring-boot:run

# Run tests (requires Docker postgres on 5432)
mvn test

# Run single test
mvn test -Dtest=InsuranceQuoteServiceTest

# Docker full stack
docker-compose up -d
```

## Spring Boot 4.x Gotchas

Test annotations moved. Wrong imports = compile failure:

| Old (3.x) | New (4.x) |
|-----------|-----------|
| `@WebMvcTest` | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
| `@MockBean` | `org.springframework.test.context.bean.override.mockito.MockitoBean` |
| `@DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |

## Timezone

PostgreSQL 18+ rejects `Asia/Calcutta`. Forces UTC via:
- `.mvn/jvm.config`: `-Duser.timezone=UTC`
- `pom.xml` spring-boot-maven-plugin `<jvmArguments>`
- `Dockerfile` ENTRYPOINT

## GoRules

- Rules live in `src/main/resources/rules/*.json` (JDM format)
- `RuleManager` uses Java WatchService for hot reload (no restart needed)
- `@ActivityMethod` goes on the **interface**, not the implementation (Temporal requirement)
- Decision table rules use column IDs (`i1`, `o1`), not field names
- Expression nodes use `expressions` array with `{id, key, value}`, not `expression` string

## Architecture

- Workflow: `InsuranceQuoteWorkflowImpl` - orchestration only, no business logic
- Activities: `ValidateActivity`, `DecisionActivity`, `PersistActivity`, `NotificationActivity`
- `DecisionActivity` calls `GoRulesEngine` which calls `RuleManager`
- `PersistActivity` calls `InsuranceQuoteService` (business layer)
- Business logic stays in `service/` and `rules/`, never in controllers or workflows

## Key Files

- `src/main/resources/rules/` - GoRules decision models (eligibility, pricing, discount)
- `src/main/java/.../config/WorkerConfig.java` - Temporal worker lifecycle (`SmartLifecycle`)
- `src/main/java/.../rules/RuleManager.java` - Rule loading + hot reload
- `docs/GORULES.md` - How to edit/test rules with GoRules BRMS UI

## Testing Rules Outside App

1. Go to https://editor.gorules.io → Import rule JSON → Simulator tab
2. Or: `mvn test -Dtest=RuleSimulatorTest#testEligibility`
