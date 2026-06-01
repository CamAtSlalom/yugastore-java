---
name: code-standards
type: context
priority: high
tool: claude
loading: on-demand
used-by: [codeReviewer, debugger, testGenerator, performanceAnalyzer, documentationAssistant, apiValidator]
---

# Context: Code Standards (Yugastore-Java)

## Summary
Conventions for the Yugastore monorepo: Spring Boot 2.6.3 / Java 17 services
under base package `com.yugabyte.app.yugastore`, plus the React 16 CRA frontend
in `react-ui/frontend`.

## Java / Spring Conventions
- **Namespace**: Spring Boot 2.6 => `javax.*` imports (e.g. `javax.persistence`,
  `javax.validation`). Do NOT migrate to `jakarta.*` — it will not compile.
- **Package layout** (per service, under `com.yugabyte.app.yugastore[.<svc>]`):
  - `config/`     — `@Configuration`, `SecurityConfiguration`, Eureka/REST setup
  - `controller/` — REST controllers (where not using Spring Data REST)
  - `service/`    — business interfaces + matching `*Impl` classes
  - `repository/` (or `repo/`) — Spring Data repositories
  - `domain/`     — entities / models (`@Entity` for YSQL, `@Table` for YCQL)
  - `exception/`  — custom exceptions + handlers
- **Dependency injection**: constructor injection (final fields). Avoid field
  `@Autowired`.
- **Service pattern**: define a `FooService` interface with a `FooServiceImpl`
  implementation; controllers/clients depend on the interface.
- **Data access**: products/checkout use Spring Data Cassandra (YCQL);
  cart/login use Spring Data JPA (YSQL). Keep repository methods derived/queries
  idiomatic to each Spring Data module.
- **Config keys**: externalize under the `cronos.yugabyte.*` namespace; default
  to the `local` profile for dev.
- **Style**: 4-space indent, standard Java naming (PascalCase types, camelCase
  members), one top-level class per file.

## React / JavaScript Conventions (react-ui/frontend)
- React 16 with Create React App (react-scripts 1.1.1); JSX in `src/`.
- Components in PascalCase files; keep presentational components small.
- The UI calls ONLY the api-gateway (port 8081) — never individual services.
- Use semantic selectors (`data-testid`, ARIA roles) to keep future Playwright
  E2E and accessibility checks stable.
- Follow the CRA/react-scripts default ESLint config; do not eject.

## Maintainability Targets (from QE-Core)
- Average cyclomatic complexity per function < 10.
- Technical debt ratio < 15% of codebase.
- Test code held to the same standards as production code.
- Critical paths documented.

## Cross-Cutting
- No credentials, tokens, or secrets in source or committed config.
- Public APIs and non-obvious logic documented.
