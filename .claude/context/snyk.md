---
name: snyk-project-context
type: context
priority: medium
tool: snyk
---

# Context: Snyk Dependency Security (Yugastore)

## Summary
Snyk is the proposed dependency- and container-security scanner for Yugastore. It
covers both ecosystems: **Maven** (Java microservices) and **npm** (React UI). Snyk
is **not yet present** in this repo — this describes how it should be used **when
introduced**. It does not authorize installing dependencies or changing the build
during workspace setup. Severity SLAs and license policy come from
`dependency-mgmt.md` and `security-policies.md`.

## Project Profile
- Maven multi-module monorepo (`./mvnw`) — root `pom.xml` aggregates services.
- npm app in `react-ui/frontend` (React 16 / CRA / react-scripts 1.1.1).
- Stack pins: Java 17, Spring Boot 2.6.3, Spring Cloud 2021.0.0 (javax.*, NOT
  jakarta.*). Do not bump majors to "fix" a CVE without testing.

## Scanning (when introduced)
- `snyk test` (or `npm audit` for the UI) on every PR; `snyk monitor` on main merges.
- Scan container images and IaC; generate an SBOM per release.
- CI threshold: `snyk test --severity-threshold=high` (Maven) or
  `npm audit --audit-level=high` (UI).

## Severity → PR action (CVSS, from dependency-mgmt.md)
| Severity | Window | PR gate |
|----------|--------|---------|
| Critical | immediately | block |
| High | 48h–7 days | block |
| Medium | within sprint / 30 days | warn |
| Low | backlog | info |

No known-vulnerable versions in production; replace EOL deps.

## Ignore/Suppression Policy
Never ignore Critical in production. Ignores require a `.snyk` entry with reason,
expiry (≤ 90 days), and approver; reviewed quarterly; removed when a fix lands.

## Upgrade Strategy
Prefer Snyk automated fix PRs; test upgrades in CI for breaking-change risk. For
transitive CVEs, upgrade the direct dependency pulling them in. `npm audit fix` for
safe non-breaking UI upgrades.

## License Compliance
Block AGPL-3.0; review GPL-2.0/3.0; allow MIT/Apache-2.0/BSD/ISC. Generate a license
report per release. (Aligns with the checkout-conversion OSS-only feature-flag
tooling decision.)

## Exceptions
Lower threshold acceptable for dev-only deps; manual schedule for pinned/vendored
deps; warn-only on pre-release/canary builds.
