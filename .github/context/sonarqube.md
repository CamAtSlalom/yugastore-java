---
name: sonarqube-project-context
type: context
priority: medium
tool: sonarqube
---

# Context: SonarQube SAST & Quality (Yugastore)

## Summary
SonarQube is the proposed static-analysis (SAST) and code-quality gate for Yugastore.
It scans the Java microservices and the React UI, enforcing a quality gate on NEW
code. SonarQube is **not yet present** in this repo — this describes how it should be
used **when introduced**. It does not authorize installing dependencies or changing
the build during workspace setup. Thresholds come from `quality-metrics.md`,
`security-policies.md`, and `code-standards.md`.

## Project Profile
- Maven multi-module monorepo (`./mvnw`); coverage from JaCoCo (Java) + Jest (UI).
- Stack pins: Java 17, Spring Boot 2.6.3 (javax.*). React 16 / CRA in
  `react-ui/frontend`.

## Quality Gate (blocks merge) — Clean as You Code
- New-code coverage ≥ 80%.
- New duplicated lines ≤ 3%.
- New-code Maintainability / Reliability / Security rating = A.
- Security hotspots 100% reviewed.

## Severity → PR action
| Severity | Window | Effect |
|----------|--------|--------|
| Blocker | immediately | blocks |
| Critical | 24h | blocks |
| Major | within sprint | blocks new code |
| Minor / Info | — | non-blocking |

## Hotspots & Suppression
- Mark hotspots Fixed/Safe/Acknowledged; Safe needs documented justification;
  Acknowledged needs a remediation ticket; all reviewed before merge.
- Never suppress Blocker/Critical. `// NOSONAR` needs team-lead approval +
  justification; review suppressions quarterly as tech debt.

## Integration (when introduced)
- Scan every PR and merge to main; upload coverage from the test step (JaCoCo for
  Java); webhook blocks merge on gate failure.
- Developers use SonarLint connected mode for local feedback.

## Exceptions
Exclude generated code (`sonar.exclusions`), test files from production metrics, and
vendored third-party code. Gate NEW code; improve legacy incrementally (boy-scout
rule); track overall project goals separately from PR gates.
