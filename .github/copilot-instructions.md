# Yugastore-Java — GitHub Copilot Instructions

> Repository-wide instructions for GitHub Copilot. This is the Copilot mirror of
> `CLAUDE.md` + `.claude/`. Path-specific rules live in `.github/instructions/`,
> SDD commands in `.github/prompts/`, agents in `.github/agents/`, context in
> `.github/context/`, consolidated rules in `.github/rules/`.

## Project Overview

Yugastore is a sample microservices eCommerce marketplace demonstrating YugabyteDB.
It is a **Maven multi-module monorepo** of Spring Boot microservices behind an API
gateway, with a React storefront.

- **Java 17**, **Spring Boot 2.6.3**, **Spring Cloud 2021.0.0**, Eureka discovery
- **Build**: Maven via the wrapper `./mvnw` (root `pom.xml` aggregates modules)
- **Frontend**: React 16, Create React App (`react-scripts`), npm — `react-ui/frontend`
- **Database**: YugabyteDB — YCQL (Cassandra-compatible) + YSQL (Postgres-compatible)
- **Data load**: Python 3 scripts in `resources/`
- **Base package**: `com.yugabyte.app.yugastore`

### Microservices & Ports

| Service | Port | Role | Data Layer |
|---------|------|------|-----------|
| eureka-server-local | 8761 | Eureka service discovery | — |
| api-gateway-microservice | 8081 | **Sole external entry point** (UI talks only here) | — |
| products-microservice | 8082 | Product catalog | YCQL (Spring Data Cassandra) |
| cart-microservice | 8083 | Shopping cart | YSQL (Spring Data JPA) |
| checkout-microservice | 8086 | Checkout / orders | YCQL |
| login-microservice | 8085 | Auth (WIP) | YSQL |

### Key Commands

- Build (skip tests): `./mvnw -DskipTests package`
- Run one service: `./mvnw spring-boot:run` (from the module dir)
- Java tests: `./mvnw test` · single module: `./mvnw -pl <module> test`
- Frontend dev / tests: `npm start` / `npm test` (in `react-ui/frontend`)
- Containerized run: `./docker-run.sh`

---

## Agent Constraints (ALWAYS APPLY)

These are non-negotiable safety boundaries for every Copilot interaction in this repo:

- **Never commit secrets**, credentials, or YugabyteDB connection strings. Config comes
  from `application.yml` / `application.properties` / env — never hardcoded literals.
- **Do not run** builds, installers, deployments, or destructive DB operations unless
  explicitly asked. Prefer `./mvnw` (never assume a global `mvn`); use `npm` in
  `react-ui/frontend`.
- **Stay within Boot 2.6 conventions**: `javax.*` imports, **NOT** `jakarta.*`. Spring
  Cloud 2021.0.0. Do not bump major framework versions opportunistically.
- **All external traffic flows through api-gateway (8081)**. Do not introduce new
  external entry points or expose other services directly.
- **Make the smallest change** that satisfies the task. Do not gold-plate or refactor
  unrelated code.
- **State confidence and reasoning** for findings; flag uncertainty rather than
  asserting. Ask before irreversible actions.
- **Never weaken or skip a quality gate** (tests, coverage, security scan) to make
  something pass.

---

## Plan Before Code (REQUIRED)

For any non-trivial change (more than a one-line edit or a single obvious fix), **plan
before you write code**:

1. **Restate the goal** and the acceptance criteria in your own words.
2. **Investigate first** — read the relevant existing code (`@workspace`, `#file`)
   before proposing changes. Identify the service, its data stack (YCQL vs YSQL), and
   the existing patterns you must match.
3. **Present a short plan** — the files you'll touch, the approach, and any trade-offs
   or risks — and get agreement before editing.
4. **Then implement** the smallest change that satisfies the plan, following the
   path-scoped instructions for the files you touch.
5. **Verify** — run the relevant tests (`./mvnw -pl <module> test` or `npm test`) and
   report results honestly.

If a task is large or spans multiple files/services, use the **Spec-Driven Development**
workflow below instead of ad-hoc edits.

---

## Spec-Driven Development (SDD) — Use For Features

This repo is set up for SDD. **Substantial work (new features, multi-file changes,
anything tied to a ticket) goes through SDD, not ad-hoc coding.** Specs live under
`.sdd/specs/<feature>/`; methodology rules and templates are in `.sdd-settings/`.

Run the SDD prompt files from `.github/prompts/` (invoke in Copilot Chat as
`/sdd-<name>`). Typical brownfield flow:

```
/sdd-spec-init "<feature or PROJ-123>"     # initialize spec (auto-detects Jira keys)
/sdd-spec-requirements <feature>           # EARS-format requirements
/sdd-validate-gap <feature>                # analyze gap vs existing code (brownfield)
/sdd-spec-design <feature> -y              # technical design
/sdd-validate-design <feature>             # design quality review
/sdd-spec-tasks <feature> -y               # break design into tasks
/sdd-spec-impl <feature> 1.1               # implement ONE task at a time (TDD)
/sdd-validate-impl <feature>               # validate implementation vs spec
```

- **One task at a time.** Clear conversation context between `spec-impl` tasks.
- **Strict phase separation** — do not jump ahead to design/tasks during init.
- `/sdd-spec-quick` runs the early phases in sequence for small specs.

The SDD agents that back these prompts live in `.github/agents/sdd-*.agent.md`.

---

## How These Instructions Load

- **This file** (`copilot-instructions.md`) is repo-wide and always applies — keep it
  the always-loaded core (project overview + constraints + plan/SDD mandates).
- **`.github/instructions/*.instructions.md`** are path-scoped via `applyTo:` globs —
  they load only when Copilot touches matching files (Java, React, Python, tests,
  controllers/repos, spec files).
- **`.github/rules/rules.md`** is the consolidated source of truth for code-review,
  testing, security, performance, API, CI/CD, and per-tool standards.
- **`.github/rules/context-dictionary.md`** maps agents → contexts → rules.
- **`.github/agents/`** — QE agents (code-reviewer, test-generator, security-scanner,
  etc.), testing-tool agents (junit5, jest, playwright), and SDD agents.
- **`.github/context/`** — project/code/quality/testing context definitions.

When in doubt about a standard, consult `.github/rules/rules.md`. When working on a
specific file type, follow its `.github/instructions/*.instructions.md`.
