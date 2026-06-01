---
name: team-structure
type: context
priority: medium
tool: claude
loading: on-demand
used-by: [mergeRequest]
---

# Context: Team Structure (Yugastore-Java)

## Summary
Roles and review authorities for Yugastore. This is a sample/reference repo with
no CONTRIBUTING.md, CODEOWNERS, or documented team; the structure below uses
QE-Core role defaults and is ADJUSTABLE once the real team is known.

## QE Operating Model (QE-Core)
- **Whole-team ownership of quality** — quality is not delegated to a QA role.
- **QE as enablers** — QE is embedded in the team, builds frameworks, pairs with
  developers, reviews testability; QE does not act as a gatekeeper.
- **Close proximity of dev and test** — testing happens throughout the story
  lifecycle, not after development.
- **Testing is an activity, not a role** — developers own unit tests; QE brings
  specialized expertise.

## Roles (defaults — adjust to actual team)
| Role | Focus | Owns |
|------|-------|------|
| Tech Lead | Architecture, cross-service design | Service contracts, gateway, data model |
| Backend Engineers | Spring Boot services | products / cart / checkout / login services |
| Frontend Engineers | React storefront | react-ui/frontend, gateway integration |
| QE Engineer | Test design + automation | Test suites, frameworks, quality gates |
| QE Lead / Architect | Test strategy, tooling | Playwright/Gatling/Snyk/SonarQube rollout |

## Team Size
Not documented in the repo. QE-Core sizing guidance (adjustable):
- Startup (1–2 QE): exploratory-dominant, partner closely with developers.
- Mid-size (3–10 QE): emerging specialization, balanced automation + manual.
- Enterprise (10+ QE): centers of excellence, embedded QE, advanced analytics.

## Review Authority (defaults)
- **Backend service change**: reviewed by a backend engineer familiar with that
  service's data layer (YCQL vs YSQL).
- **Frontend change**: reviewed by a frontend engineer.
- **Shared/gateway/contract change**: cross-area review required.
- **QE participates in PR reviews** for testability and test quality.

## QE Code Review Checklist (QE-Core)
- [ ] Unit tests comprehensive and deterministic?
- [ ] New lines/functions covered?
- [ ] Clear test names describing behavior?
- [ ] Meaningful assertion failure messages?
- [ ] Representative, isolated test data?
- [ ] Appropriate mocking (not over-mocking)?

## Escalation
Discuss with team -> QE Lead / Tech Lead -> Director / VP Engineering.
Document risk-acceptance decisions. QE escalates concerns but holds no veto.
