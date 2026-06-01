---
name: dependency-mgmt
type: context
priority: medium
tool: claude
loading: on-demand
used-by: [securityScanner]
---

# Context: Dependency Management (Yugastore-Java)

## Summary
How dependencies and their vulnerabilities are governed across the two ecosystems
in this repo: **Maven** (Java microservices, via `./mvnw`) and **npm** (React UI in
`react-ui/frontend`). Derived from `.claude/rules/rules.md` (Snyk standards).

## Ecosystems
- **Maven**: root `pom.xml` (`<packaging>pom</packaging>`) aggregates modules; each
  microservice inherits from it. Add cross-cutting deps/versions at the root — do
  not bump major framework versions opportunistically.
- **npm**: `react-ui/frontend` (React 16, Create React App / react-scripts 1.1.1).

## Stack pins (do not drift)
- Java 17, Spring Boot 2.6.3, Spring Cloud 2021.0.0. Boot 2.6 ⇒ `javax.*` (NOT
  `jakarta.*`). A `jakarta.*` import is a build break, not an upgrade.

## CVE Severity → PR action (CVSS)
| CVSS | Window | PR gate |
|------|--------|---------|
| 9.0–10.0 (Critical) | immediately | block (CISO to override) |
| 7.0–8.9 (High) | 48h–7 days | block (security-lead to override) |
| 4.0–6.9 (Medium) | within sprint / 30 days | warn |
| < 4.0 (Low) | backlog / track | info |

No known-vulnerable versions in production; replace EOL dependencies.

## Scanning (Snyk — to be introduced)
- `snyk test` (or `npm audit` for the UI) on every PR; `snyk monitor` on main merges.
- CI threshold: `snyk test --severity-threshold=high` (Maven) or
  `npm audit --audit-level=high` (UI). Generate an SBOM per release.
- Prefer Snyk automated fix PRs; test upgrades in CI for breaking-change risk. For
  transitive CVEs, upgrade the direct dependency pulling them in.

## Ignore/suppression policy
Never ignore Critical in production. Ignores require a `.snyk` entry with reason,
expiry (≤ 90 days), and approver; reviewed quarterly; removed when a fix lands.

## License compliance
Block AGPL-3.0; review GPL-2.0/3.0; allow MIT/Apache-2.0/BSD/ISC. Generate a license
report per release. (Relevant to the checkout-conversion decision to use only
OSS feature-flag tooling — avoid copyleft/SaaS lock-in.)

## Current State
No Snyk/`npm audit` gate is configured yet. These are the to-be standards.
