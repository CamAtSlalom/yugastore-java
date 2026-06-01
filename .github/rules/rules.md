# Yugastore-Java — Consolidated Rules (Claude Code)

> QE-AI Accelerator RULES file. Tool target: Claude Code. Mode: AI Workspace.
> Source of truth: `ai-qe-accelerator/AI-Accelerator/Foundation/context-dictionary.md` (Rules Registry + Claude path-scoping). QE-Core quality gates and coverage thresholds (`QE-Core/quality-standards.md`, `QE-Core/testing-strategy.md`) take precedence over any inferred values below.

## Project Profile (for concreteness)

- **Build**: Maven multi-module monorepo, wrapper `./mvnw`. Java 17, Spring Boot 2.6.3, Spring Cloud 2021.0.0.
- **Boot 2.6 ⇒ `javax.*` imports, NOT `jakarta.*`.** Do not migrate namespaces.
- **Base package**: `com.yugabyte.app.yugastore`.
- **Microservices**: eureka-server-local (8761) · api-gateway (8081, the *only* external entry point) · products (8082, YCQL / Spring Data Cassandra) · cart (8083, YSQL / Spring Data JPA) · checkout (8086, YCQL) · login (8085, YSQL).
- **Data**: YugabyteDB YCQL + YSQL, exposed via Spring Data REST.
- **UI**: `react-ui/frontend` — React 16 / Create React App / npm; Jest runs via `react-scripts test`.
- **Existing tests**: JUnit + Jest. No E2E / performance / security / CI-CD pipeline yet.

---

## How These Rules Load in Claude Code

`agent-constraints` below is the **always-loaded core** (no path scope — applies every session). All other rule sections are **path-scoped**: treat them as active only when working on files matching their declared paths. This keeps each session lean (core + the 1-2 rules relevant to the files in play) per the dictionary's loading budget (core < 2K tokens; each path-scoped rule < 1K tokens).

---

## Rule: agent-constraints  *(ALWAYS-LOADED CORE)*

**Loading**: Every session, unconditionally. Keep this section small.

- Never commit secrets, credentials, or YugabyteDB connection strings. Config comes from `application.properties` / env, not hardcoded literals.
- Do not run builds, installers, deployments, or destructive DB operations unless explicitly asked. Prefer `./mvnw` (never assume a global `mvn`); use `npm` in `react-ui/frontend`.
- Stay within Boot 2.6 conventions: `javax.*`, Spring Cloud 2021.0.0. Do not bump major framework versions opportunistically.
- All external traffic flows through api-gateway (8081). Do not introduce new external entry points or expose other services directly.
- Make the smallest change that satisfies the task; do not gold-plate or refactor unrelated code.
- State confidence and reasoning for findings; flag uncertainty rather than asserting. Ask before irreversible actions.
- Never weaken or skip a quality gate (tests, coverage, security scan) to make something pass.

---

## Rule: code-review-standards

**Path scope**: `*/src/**` (all microservice source), `react-ui/frontend/src/**`.
**Applied by**: codeReviewer, mergeRequest, testGenerator, apiValidator.

- **Coverage gate**: ≥ 80% on new code (QE-Core release gate); critical paths (login/auth, cart, checkout) ≥ 90%. Focus on critical-path coverage, not raw line count.
- **Maintainability**: average cyclomatic complexity per function < 10; complex functions ≤ 20 lines or carry reasoning comments. Technical-debt ratio target < 15%.
- **No dead/commented-out code** — rely on git history.
- **Public methods / APIs**: document intent, usage, and design rationale.
- **Reasoning-first review**: explain *why* each finding matters; attach a confidence score (0.0-1.0).
  - 0.8-1.0: must justify with a code example and reference to a standard.
  - 0.5-0.8: flag for discussion, offer alternatives.
  - < 0.5: mention, do not block; defer to code owner.
- **Security in review**: no hardcoded secrets; validate all external inputs; parameterized queries only (YCQL/YSQL via Spring Data — never concatenate query strings). Distinguish authentication vs authorization (relevant to the login service).
- **QE-Core PR gates**: unit + component/API tests pass 100%; static analysis passes; QE participates in PR review; coverage threshold met. Failing tests block promotion — no exceptions.
- **Exceptions**: auto-generated code (Lombok/MapStruct), third-party wrappers (review our adapter only), docs-only changes (no test requirement), incident hotfixes (lower coverage allowed, full review after).

---

## Rule: testing-standards

**Path scope**: `**/*Test*.java`, `**/*IT.java`, `**/*.test.*`, `**/*.spec.*`, `**/__tests__/**`.
**Applied by**: testGenerator, debugger.

- **Frameworks**: JUnit 5 for Java/Spring services; Jest (via `react-scripts`) for the React UI. Validate and uphold the existing setup — do not replace frameworks.
- **Testing pyramid** (QE-Core): Unit 60-75% · Component/API/Integration 20-30% · E2E/UI 5-10%. Push tests as low as possible.
- **Coverage**: ≥ 80% new code (critical paths ≥ 90% branch). Integration/API coverage > 70%.
- **Determinism**: tests must be atomic, independent, and deterministic. No reliance on timing or pre-existing data. Flakiness target < 1% — fix flaky tests, never ignore.
- **Test data**: clean state before each test; use in-memory/embedded or containerized DBs for integration; never test against production data; no assumptions about seeded YugabyteDB rows.
- **Speed**: unit suite fast feedback (< ~5 min); Spring integration tests sliced and bounded.
- **Diagnostics**: failures must surface a clear root cause (QE-Core diagnostic-quality target > 90%); P1/P2 bugs reproducible in 1 attempt.
- See **Testing Tool Rules** below for JUnit5 / Jest specifics.

---

## Rule: security-standards

**Path scope**: `*/src/**`, `react-ui/frontend/src/**`.
**Applied by**: codeReviewer, securityScanner, cicdValidator.

- **OWASP Top 10**: 100% tested coverage as a goal. Priority areas: broken access control, injection, auth failures, cryptographic failures, vulnerable components.
- **Injection**: parameterized queries only across YCQL (Cassandra) and YSQL (JPA). Never build queries via string concatenation. Validate/sanitize all inputs reaching api-gateway and downstream services.
- **Secrets**: no API keys, passwords, or DB credentials in code or committed config — use environment/secrets management. Never log PII, tokens, or credentials.
- **Auth (login service, 8085)**: passwords hashed (bcrypt/scrypt); rate-limit failed attempts; sessions/tokens expire and are validated; prevent replay and privilege escalation.
- **Transport/data**: HTTPS/TLS 1.2+ in transit; encryption at rest AES-256 or equivalent; identify and handle PII.
- **Severity SLAs** (QE-Core): Critical vulnerabilities = 0 (block merge / fix immediately); High patched within 7 days; Medium within 30 days; Low tracked.
- **Dependency CVEs (CVSS)**: 9.0-10.0 fix immediately · 7.0-8.9 within 7 days · 4.0-6.9 within 30 days · < 4.0 track. No known-vulnerable versions in production; replace EOL deps.
- **Findings**: cite vulnerability type (CWE) and explain the attack vector.
- See **Testing Tool Rules** (SonarQube, Snyk) below.

---

## Rule: performance-standards

**Path scope**: `*/src/**`, `react-ui/frontend/src/**`, performance simulation/plan files.
**Applied by**: performanceAnalyzer.

- **API latency (QE-Core)**: p95 < 500ms (baseline 200ms), p99 < 1000ms (baseline 500ms). Investigate / may block deployment if exceeded.
- **UI page load**: p95 < 2s (target), < 3s ceiling.
- **Throughput / errors**: sustain peak without degradation (>1000 req/min or app-specific); error rate < 0.1% at peak; CPU < 80% at peak; memory leak < 10 MB/hour.
- **Regression gate**: no > 10% performance regression vs baseline.
- **Common pitfalls to avoid**: N+1 queries (watch Spring Data REST repository access patterns across products/cart/checkout), synchronous work on critical paths, missing caching, oversized payloads (paginate Spring Data REST collections), unbounded result sets from YCQL/YSQL.
- **Test types**: load, stress, soak (4-24h, find leaks/pool exhaustion), spike. Begin performance testing early; define SLAs upfront.
- See **Testing Tool Rules** (Gatling, JMeter) below.

---

## Rule: api-design-standards

**Path scope**: controllers, repositories, and DTOs (`**/*Controller.java`, `**/*Repository.java`, `**/*Resource.java`), and the api-gateway module.
**Applied by**: apiValidator, documentationAssistant.

- **Single entry point**: external clients call api-gateway (8081) only. Inter-service routing goes through Eureka service discovery (8761) — do not hardcode host:port for peer services.
- **REST conventions**: Spring Data REST exposes repositories — keep resource paths, HAL/`_links`, and pagination contracts consistent. Use proper HTTP verbs and status codes.
- **Backward compatibility**: API contracts validated for backward compatibility before release (QE-Core automated gate). Additive change preferred; deprecate before removing.
- **Validation**: validate request bodies/params; return a consistent error response shape across services.
- **Per-service data semantics**: products/checkout = YCQL (Spring Data Cassandra), cart/login = YSQL (Spring Data JPA). Respect each store's consistency and key model in endpoint design.
- **Documentation**: document public endpoints with rationale, examples, and known limitations.

---

## Rule: documentation-standards

**Path scope**: `docs/**`, `**/*.md`.
**Applied by**: documentationAssistant.

- **Required living docs**: Test Strategy (stakeholder audience), Automation Strategy (technical audience). Manual-test cases only (in story or attached).
- **Prohibited**: per-feature/epic test-plan documents, comprehensive regression test-plan docs, redundant documentation of automated tests.
- **Code examples** in docs must be runnable against this stack (`./mvnw`, `npm`, correct ports/services).
- **API docs**: include design rationale, common usage examples, and documented limitations/edge cases.
- Keep README and module docs in sync with actual service ports, package names, and build commands.

---

## Rule: ci-cd-standards

**Path scope**: `.github/workflows/**`, `**/*.yml` pipeline configs.
**Applied by**: cicdValidator, mergeRequest.
**Note**: no CI/CD pipeline exists yet — these are the standards to apply when one is introduced.

- **Pipeline stages** (QE-Core execution pipeline): lint + unit tests (< 2 min) → integration tests (< 5 min) → SAST (< 2 min) → build artifact → staging deploy → smoke/exploratory → performance baseline → production.
- **Automated release gates (cannot be overridden)**: 100% unit tests pass · new-code coverage ≥ 80% · integration tests 100% · no critical SAST findings · no critical/high CVEs · build succeeds · API backward compatibility validated.
- **Manual gates (leadership override)**: security review (high-risk), performance testing (no > 10% regression), UAT, QA lead sign-off, PO approval.
- **Rollout gates**: canary error rate < baseline · monitoring dashboards in place · rollback procedure verified · on-call ready.
- **Failing tests block artifact promotion — no exceptions.** Build with `./mvnw` for Java modules and `npm`/`react-scripts` for the UI; manage the multi-module reactor and per-service builds.

---

# Testing Tool Rules

Workspace standards an engineer or agent must follow when using each selected tool. **Standards only — no config files, dependency installs, or scaffolding.** JUnit5 + Jest already exist (validate & uphold). Playwright, Gatling, JMeter, SonarQube, and Snyk are standards to apply when those tools are introduced.

## JUnit 5 — validate & uphold the existing setup

- **Structure**: `@Nested` to group by method/scenario; `@DisplayName` on every class and method; Arrange-Act-Assert; one logical assertion per test (use `assertAll` for related checks). `@BeforeEach` per-test, `@BeforeAll` one-time.
- **Naming**: classes `*Test.java`; integration tests `*IntegrationTest.java` / `*IT.java`; mirror `src/main/java` package layout under `src/test/java`.
- **Assertions**: prefer AssertJ fluent (`assertThat(actual).isEqualTo(expected)`); exceptions via `assertThatThrownBy(...).isInstanceOf(...)`; never `assertTrue(a.equals(b))`.
- **Mocking**: `@ExtendWith(MockitoExtension.class)`; `@Mock` for deps, `@InjectMocks` for the unit under test; `when(...).thenReturn(...)` / `verify(...)`; `@Spy` only for partial mocking; never mock value objects/DTOs.
- **Parameterized**: `@ValueSource` (single param), `@CsvSource` (multi), `@MethodSource` (complex data), `@EnumSource`; always name for readable output.
- **Spring slices**: prefer slices over full `@SpringBootTest` — `@WebMvcTest` (controllers), `@DataJpaTest` (YSQL/JPA repositories), `@MockBean` to replace context beans. Reserve `@SpringBootTest` for true integration (expensive).
- **Speed**: unit < 5s each; Spring integration < 30s each; full unit suite < 5 min; `@Tag("slow")` to exclude long tests by default.
- **Coverage (JaCoCo)**: new code ≥ 80% line; critical paths ≥ 90% branch; exclude DTOs, config, generated code; enforce via JaCoCo plugin (fail build below threshold).
- **CI**: Surefire for `mvn test` (unit), Failsafe for `mvn verify` (integration), JUnit XML reports.
- **Exceptions**: exclude Lombok/MapStruct-generated code; DTOs/POJOs need tests only when they contain logic; migrate legacy JUnit 4 incrementally via `junit-vintage-engine`.

## Jest — validate & uphold the existing setup (`react-ui/frontend`)

- **Structure**: `describe` to group; `it`/`test` names that read as sentences; AAA pattern; one assertion concept per test; no shared mutable state between tests.
- **Naming**: `*.test.*` or `*.spec.*` (be consistent); describe = unit name; test names start with "should"/behavior; manual mocks in `__mocks__/`.
- **Mocking**: `jest.fn()` for functions, `jest.mock('module')` for modules, `jest.spyOn()` to observe while keeping behavior (restore in `afterEach`). Mock boundaries (APIs, storage), not internals. `jest.useFakeTimers()` for time-based code.
- **Snapshots**: descriptive names; review diffs in PRs (never blind-update); inline snapshots (< 10 lines) for reviewability; scope to a specific element; fail on obsolete snapshots in CI (`--ci`).
- **Async**: always `await`/return Promises; `async/await` over `.then()`; `waitFor()` for async DOM; avoid the `done` callback.
- **React components**: `@testing-library/react` (not Enzyme); query by role → label → text → testId (last resort); test behavior not implementation; prefer `userEvent` over `fireEvent`.
- **Speed**: each test < 5s; suite < 60s; `--maxWorkers=50%` in CI.
- **Coverage**: new code ≥ 80% branch; critical paths ≥ 90%; utilities ≥ 95%; exclude generated code.
- **CI**: `--ci`, `--bail`; upload coverage artifacts; `--forceExit` only after investigating open handles. Test failures block merge.

## Playwright — standards to apply when introduced (E2E)

- **Locators**: accessible first — `getByRole`/`getByLabel`/`getByText`/`getByPlaceholder`; `getByTestId` only as last resort; never CSS/XPath for user-facing elements. Locators auto-wait.
- **Waits**: never `waitForTimeout`/sleep; use auto-retrying `expect(locator).toBeVisible()/toHaveText()`; `waitForURL()` after navigation; `waitForResponse()` for API-driven UI (route through api-gateway:8081).
- **Isolation**: independent tests; `beforeEach` for navigation, not chaining; reuse auth via storage state; `serial` mode only for truly sequential flows.
- **Page Object Model**: POM for pages with > 3 interactions; POMs encapsulate locators; keep them thin (business logic in tests).
- **Visual regression**: `toHaveScreenshot()` with names + `maxDiffPixels`/`maxDiffPixelRatio`; single browser (Chromium) for visual; update snapshots deliberately; version-control screenshots.
- **Accessibility**: `@axe-core/playwright` on key pages; fail on critical WCAG 2.1 AA violations.
- **CI**: `retries: 2`, `trace: 'on-first-retry'`, `screenshot: 'only-on-failure'`, parallel workers, `--shard`; upload HTML report + traces.
- **Speed**: each test < 30s; full suite < 10 min; use `webServer` to auto-start the app.

## Gatling — standards to apply when introduced (performance)

- **Structure**: extend `Simulation`, call `setUp()`; define `httpProtocol` (base URL via api-gateway:8081, headers, connection); `scenario()` for journeys; always include `assertions()`.
- **Injection profiles**: use ramp-up (`rampUsers`, `constantUsersPerSec` for arrival-rate) — `atOnceUsers()` only for smoke; include think-time pauses; match production traffic shape.
- **Assertions (required, drive CI exit code)**: p95 `global.responseTime.percentile3.lt(500)` aligned to QE-Core (< 500ms); success rate `global.successfulRequests.percent.gt(99.0)`; per-request assertions on critical endpoints.
- **Data**: feeders for all parameterized data (CSV/JSON/JDBC) in `src/test/resources/feeders/`; `circular`/`random` strategies as appropriate.
- **Checks**: validate status (`status.is(200)`); extract/save session vars (`jsonPath(...).saveAs(...)`); validate content.
- **Reporting**: review HTML report each run; focus on p95/p99 (not averages); compare to baseline; archive as CI artifact.
- **CI**: Maven/Gradle Gatling plugin; smoke sims on every PR, full load pre-release; parse assertion exit code for pass/fail.

## JMeter — standards to apply when introduced (performance)

- **Test plan**: response assertions for functional correctness under load; CSV Data Set Config (no hardcoded data); Constant/Gaussian timers for think time; Transaction Controllers to group user actions.
- **Execution**: never load-test in GUI mode (design only); CLI `jmeter -n -t plan.jmx -l results.jtl`; remove graphical listeners before CLI runs; parameterize via `-Jproperty=value`; generate report with `-e -o report/`.
- **Thread groups**: always set ramp-up (~1s/thread small tests); bounded loop count/duration (no infinite loops in CI); Stepping Thread Group for controlled ramps.
- **Memory**: tune heap (`JVM_ARGS="-Xmx4g -Xms2g"`); Simple Data Writer over View Results Tree; watch that the JMeter client isn't the bottleneck.
- **Reporting**: HTML dashboard; optional Backend Listener → InfluxDB/Grafana; report p50/p90/p95/p99, error rate, throughput; compare to baseline.
- **CI**: smoke (low load) per PR, full load pre-release; parse JTL for pass/fail; store reports as artifacts; trend over time.
- **Distributed**: identical JMeter version, test data, and properties across agents; low-latency controller↔agent network.

## SonarQube — standards to apply when introduced (SAST / quality)

- **Quality gate (blocks merge)**: new-code coverage ≥ 80%; new duplicated lines ≤ 3%; new Maintainability/Reliability/Security rating A; security hotspots 100% reviewed.
- **Severity → PR**: Blocker fix immediately (blocks) · Critical within 24h (blocks) · Major within sprint (blocks new code) · Minor/Info non-blocking.
- **Hotspots**: mark Fixed/Safe/Acknowledged; Safe requires documented justification; Acknowledged requires a remediation ticket; all reviewed before merge.
- **Suppression**: never suppress Blocker/Critical; `// NOSONAR` needs team-lead approval + justification; review suppressions quarterly as tech debt.
- **Clean as You Code**: gate NEW code; improve legacy incrementally (boy-scout rule); track overall project goals separately from PR gates.
- **Integration**: scan every PR and merge to main; upload coverage from the test step (JaCoCo for Java); webhook blocks merge on gate failure; developers use SonarLint connected mode for local feedback.
- **Exceptions**: exclude generated code (`sonar.exclusions`), test files from production metrics, and vendored third-party code.

## Snyk — standards to apply when introduced (dependency security)

- **Scanning**: `snyk test` (or `npm audit` for the React UI) on every PR; `snyk monitor` on main merges; scan container images and IaC; generate an SBOM per release.
- **Severity → PR**: Critical block (immediate, CISO to ignore) · High block (48h, security-lead to ignore) · Medium warn (within sprint, team-lead) · Low info (backlog, developer).
- **Ignore/suppress**: never ignore Critical in production; ignores require a `.snyk` entry with reason, expiry (≤ 90 days), and approver; review quarterly; remove when a fix lands.
- **Upgrade strategy**: prefer Snyk automated fix PRs; test upgrades in CI (breaking-change risk); for transitive CVEs upgrade the direct dependency pulling them in; `npm audit fix` for safe non-breaking upgrades.
- **License compliance**: block AGPL-3.0; review GPL-2.0/3.0; allow MIT/Apache-2.0/BSD/ISC; generate a license report per release.
- **Monitoring**: enable on production repos; alert security channel; review new alerts within 24h.
- **CI**: `snyk test --severity-threshold=high` (Java/Maven) or `npm audit --audit-level=high` (UI); upload SBOM; trend vulnerability counts.
- **Exceptions**: lower threshold acceptable for dev-only deps; manual schedule for pinned/vendored deps; warn-only on pre-release/canary builds.
