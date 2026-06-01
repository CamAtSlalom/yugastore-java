---
name: testing-frameworks
type: context
priority: high
tool: claude
loading: on-demand
used-by: [debugger, testGenerator]
---

# Context: Testing Frameworks (Yugastore-Java)

## Summary
Existing tests: JUnit 5 via spring-boot-starter-test (Java services) and Jest via
react-scripts (React UI). Recommended additions: Playwright (E2E), Gatling/JMeter
(performance). Snyk + SonarQube for security/SAST (see security-policies).

## Existing — Java (JUnit 5)
- **Framework**: JUnit 5 bundled in `spring-boot-starter-test` (also brings
  AssertJ, Mockito, Spring Test, JSONassert).
- **Layout**: `src/test/java/com/yugabyte/app/yugastore/...` mirroring main.
- **Run**: `./mvnw test` (all modules) or per-module from its directory.
- **Conventions**:
  - Class names end in `Test` (unit) / `IT` (integration).
  - Use `@SpringBootTest` for context/integration; slice tests
    (`@WebMvcTest`, `@DataJpaTest`) where possible for speed.
  - Mock collaborators with Mockito; prefer constructor-injected fakes.
  - Tests must be deterministic and independent (clean state per test).
  - Namespace is `javax.*` (Boot 2.6) — match in test code.

## Existing — React (Jest)
- **Framework**: Jest via `react-scripts test --env=jsdom` (CRA 1.1.1).
- **Layout**: `*.test.js` / `__tests__/` co-located with components in `src/`.
- **Run**: `cd react-ui/frontend && npm test` (interactive) or `CI=true npm test`.
- **Conventions**: render components, assert behavior; prefer semantic queries
  (role/testid) over brittle DOM selectors.

## Recommended Additions
### Playwright (E2E — React UI)
- Owns critical user journeys against the running app (UI -> api-gateway:8081).
- Keep to 5–10% of the pyramid; critical paths only (browse -> cart -> checkout).
- Use semantic selectors (`data-testid`, roles); isolate test data; run in
  parallel; fix flakiness immediately.

### Gatling / JMeter (Performance — APIs)
- Load/stress/soak/spike against gateway and service endpoints.
- See `performance-baselines` for targets and the (not-yet-captured) baselines.

## Test Pyramid Targets (QE-Core)
- Unit 60–75% (JUnit + Jest) | Component/API 20–30% | E2E 5–10% (Playwright).
- Push tests as low in the pyramid as possible.

## Determinism (QE-Core)
- Flakiness < 1%; full unit suite fast feedback; one logical assertion per test
  where practical; clear, behavior-describing test names.
- Component/integration tests use containerized/ephemeral YugabyteDB
  (Testcontainers) for YCQL and YSQL; never rely on shared mutable state.
