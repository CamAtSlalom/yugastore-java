---
name: jmeter-project-context
type: context
priority: medium
tool: jmeter
---

# Context: JMeter Performance Environment (Yugastore)

## Summary
Apache JMeter is the proposed GUI/XML-based load-testing tool for Yugastore's HTTP
APIs. Test plans (`.jmx`) are designed in the GUI and executed headless via CLI.
JMeter is **not yet present** in this repo — this describes how it should be used
**when introduced**. It does not authorize installing dependencies or changing the
build during workspace setup. SLAs come from `performance-baselines.md`.

## Project Profile
- Drive all load through the **api-gateway (8081)** — the sole external entry point.
  Never target individual microservices (products 8082, cart 8083, checkout 8086,
  login 8085) directly.
- Backend is Spring Boot 2.6.3 / Java 17 over YugabyteDB (YCQL + YSQL).
- Critical paths: catalog reads, add-to-cart (YSQL), checkout/order (YCQL),
  `GET /inventory/{asin}` scarcity.

## SLAs (from performance-baselines.md / rules.md)
- p95 < 500 ms; p99 < 1000 ms; success > 99.0%; error rate < 0.1% at peak.
- No > 10% regression vs baseline.

## Test-Plan Standards (when introduced)
- Response Assertions for functional correctness under load.
- CSV Data Set Config for all data (no hardcoded values).
- Constant/Gaussian timers for think time; Transaction Controllers to group actions.
- Thread Groups: always set ramp-up (~1s/thread small tests); bounded loop count /
  duration (no infinite loops in CI); Stepping Thread Group for controlled ramps.

## Execution (always headless for load)
```bash
jmeter -n -t plan.jmx -l results.jtl -e -o report/   # CLI run + HTML dashboard
```
- Never load-test in GUI mode (design only). Remove graphical listeners before CLI
  runs; parameterize via `-Jproperty=value`.
- Tune heap: `JVM_ARGS="-Xmx4g -Xms2g"`; prefer Simple Data Writer over View Results
  Tree; ensure the JMeter client isn't the bottleneck.

## Reporting & CI
- HTML dashboard; optional Backend Listener → InfluxDB/Grafana. Report
  p50/p90/p95/p99, error rate, throughput; compare to baseline.
- Smoke (low load) per PR, full load pre-release; parse the JTL for pass/fail; store
  reports as artifacts; trend over time.
- Distributed runs: identical JMeter version, test data, and properties across agents.
