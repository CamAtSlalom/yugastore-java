---
name: performance-baselines
type: context
priority: high
tool: claude
loading: on-demand
used-by: [performanceAnalyzer]
---

# Context: Performance Baselines & SLAs (Yugastore-Java)

## Summary
Performance targets, regression gates, and known pitfalls for Yugastore. Derived
from `.claude/rules/rules.md` (performance-standards) and QE-Core SLAs. No load
testing exists yet — these are the to-be baselines to capture and defend.

## Latency SLAs
| Surface | Target | Ceiling |
|---------|--------|---------|
| API p95 | < 500 ms (baseline 200 ms) | — |
| API p99 | < 1000 ms (baseline 500 ms) | — |
| UI page load p95 | < 2 s | < 3 s |

Investigate / may block deployment if exceeded.

## Throughput & Resource
- Sustain peak without degradation (> 1000 req/min or app-specific).
- Error rate < 0.1% at peak; CPU < 80% at peak; memory leak < 10 MB/hour.

## Regression Gate
- No > 10% performance regression vs baseline (blocks promotion when CI is wired).

## Known Pitfalls (watch in this codebase)
- **N+1 / fan-out**: Spring Data REST repository access across products/cart/
  checkout. Specifically, the product-detail path issues a duplicate 2N fan-out —
  collapse to a single read pass (tracked in checkout-conversion Phase 2).
- Synchronous/blocking work on request critical paths.
- Missing caching — the scarcity endpoint must use a short-TTL cache (Caffeine,
  in-process, default TTL 5s) with invalidation on checkout decrement.
- Oversized payloads / unbounded result sets from YCQL/YSQL — paginate.

## Test Types & Tooling (Gatling/JMeter — to be introduced)
- Load, stress, soak (4–24h to find leaks/pool exhaustion), spike.
- Begin performance testing early; define SLAs upfront; compare every run to baseline.
- Gatling: p95 assertion `global.responseTime.percentile3.lt(500)`, success-rate
  `global.successfulRequests.percent.gt(99.0)`; route through api-gateway:8081.
- Focus on p95/p99, not averages. Archive HTML reports as CI artifacts.

## Current State
No performance tooling or captured baselines exist. First task is to capture
baselines (per the protocol in `docs/SOLUTION_ARCHITECTURE_REPORT.md`) before
defending the SLAs above.
