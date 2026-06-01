---
name: ci-cd-pipeline
type: context
priority: high
tool: claude
loading: on-demand
used-by: [mergeRequest, cicdValidator]
---

# Context: CI/CD Pipeline (Yugastore-Java)

## Summary
There is currently NO CI/CD in this repository. No `.github/workflows/`, no
Jenkinsfile, no GitLab/CircleCI config exists. The section below describes the
TARGET (to-be) pipeline to stand up as part of the accelerator.

## Current State
- No automated build, test, scan, or deploy pipeline.
- Builds and tests are run locally via `./mvnw` and `npm test`.
- Containerization exists only as `./docker-run.sh` for local/manual runs.

## Target Pipeline (GitHub Actions — to be created)
Recommended workflow under `.github/workflows/`:

1. **Lint & Unit Tests** (< 2 min)
   - Java: `./mvnw -B test` across all modules.
   - Frontend: `cd react-ui/frontend && CI=true npm test`.
2. **Integration / Component Tests** (< 5 min)
   - `./mvnw -B verify`. Use Testcontainers / ephemeral YugabyteDB for YCQL +
     YSQL integration where feasible.
3. **Security Scanning (SAST + deps)** (< 2 min)
   - SonarQube (SAST) on Java + JS.
   - Snyk dependency scan (Maven + npm). See `security-policies` / `dependency-mgmt`.
4. **Build Artifacts**
   - `./mvnw -DskipTests package` to produce service jars.
   - Build container images per service (extend `docker-run.sh` / Dockerfiles).
5. **Performance Tests** (pre-release / nightly)
   - Gatling or JMeter against api-gateway / service endpoints; compare to
     baselines in `performance-baselines`.

## Quality Gates (QE-Core — enforce in pipeline)
Automated, non-overridable:
- All unit tests pass (100%); integration tests pass (100%).
- Code coverage >= 80% on new code.
- No critical SAST findings; no critical/high dependency CVEs.
- Build succeeds; API contracts backward-compatible.

Failing tests block artifact promotion — no exceptions.

## Branch Protection (to configure)
- Require passing checks (build, unit, integration, SAST, dependency scan).
- Require QE participation in PR review.
- Block merges on coverage-threshold or critical-finding failures.

## Deployment Strategy (to-be)
Stage -> Prod with smoke tests, then canary/staged rollout (5% -> 25% -> 50% ->
100%), post-deploy monitoring, and a verified rollback procedure (DB migration +
API version compatibility).
