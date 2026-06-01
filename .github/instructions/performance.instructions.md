---
applyTo: "**/src/**,react-ui/frontend/src/**"
---

# Performance Standards (Yugastore)

**Tool:** GitHub Copilot · **Source of truth:** `.github/rules/rules.md` (performance-standards, Gatling/JMeter tool rules)

- **API latency**: p95 < 500ms (baseline 200ms), p99 < 1000ms (baseline 500ms).
- **UI page load**: p95 < 2s target, < 3s ceiling.
- **Throughput / errors**: sustain peak without degradation; error rate < 0.1% at peak;
  CPU < 80% at peak; memory leak < 10 MB/hour.
- **Regression gate**: no > 10% regression vs baseline.
- **Common pitfalls to avoid**:
  - N+1 queries — watch Spring Data REST repository access patterns across
    products/cart/checkout.
  - Synchronous work on critical paths; missing caching.
  - Oversized payloads — **paginate Spring Data REST collections**.
  - Unbounded result sets from YCQL/YSQL.
- **Test types**: load, stress, soak (4-24h, find leaks/pool exhaustion), spike. Define
  SLAs upfront; begin performance testing early.

For analysis, invoke the `performance-analyzer` agent (`.github/agents/performance-analyzer.md`).
