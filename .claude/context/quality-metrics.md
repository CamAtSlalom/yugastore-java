---
name: quality-metrics
type: context
priority: high
tool: claude
loading: on-demand
used-by: [codeReviewer, mergeRequest]
---

# Context: Quality Metrics (Yugastore-Java)

## Summary
Coverage targets, quality gates, and success metrics for Yugastore. Values are
the QE-Core organizational defaults; repo currently captures no metrics, so
these are the to-be targets (adjustable per team agreement).

## Coverage Targets (QE-Core)
| Metric | Target |
|--------|--------|
| Unit test coverage (critical paths) | > 80% (new code) |
| Integration coverage (APIs/services) | > 70% |
| Acceptance criteria verification | 100% before release |
| Defect escape rate | < 2% |
| Test flakiness | < 1% |

Critical paths for Yugastore: cart operations (YSQL), checkout/order flow (YCQL),
product catalog reads, and the api-gateway routing layer.

## Quality Gates
### Automated (cannot be overridden)
- [ ] All unit tests pass — JUnit (`./mvnw test`) + Jest (`npm test`)
- [ ] Code coverage >= 80% on new code
- [ ] Integration tests pass (100%)
- [ ] No critical SAST findings (SonarQube — to be added)
- [ ] No critical/high CVEs in dependencies (Snyk — to be added)
- [ ] Build succeeds (`./mvnw -DskipTests package` then full `./mvnw verify`)
- [ ] API contracts validated (backward compatibility on gateway/REST resources)

### Manual review gates (overridable by leadership)
- [ ] Security review for high-risk changes (auth/login-microservice)
- [ ] Performance testing passed — no > 10% regression (Gatling/JMeter — to be added)
- [ ] QE lead sign-off on test execution
- [ ] Product owner approval

## Test Pyramid Distribution (target)
- Unit (JUnit/Jest): 60–75%
- Component/API/integration: 20–30%
- E2E (Playwright — to be added): 5–10%, critical user paths only

## Success Metrics (QE-Core)
| Metric | Target |
|--------|--------|
| Defect escape rate | < 2% |
| Test flakiness | < 1% |
| Automation coverage (critical paths) | > 80% |
| S1 production incidents (monthly trend) | Declining |
| Cycle time (commit -> prod for fixes) | < 4 hours |

## Current State
No coverage tooling (JaCoCo, coverage thresholds) is configured yet. Existing
tests are JUnit (spring-boot-starter-test) and Jest. Establishing baselines and
wiring coverage into CI is part of the accelerator rollout.
