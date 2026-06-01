---
name: gatling-project-context
type: context
priority: medium
tool: gatling
---

# Context: Gatling Performance Environment (Yugastore)

## Summary
Gatling is the proposed code-based load-testing tool for Yugastore's HTTP APIs.
Scenarios are written in Scala/Java as `Simulation` classes and driven by injection
profiles. Gatling is **not yet present** in this repo — this describes how it should
be used **when introduced**. It does not authorize installing dependencies or
changing the build during workspace setup. SLAs come from `performance-baselines.md`.

## Project Profile
- Drive all load through the **api-gateway (8081)** — the sole external entry point.
  Never load-test individual microservices (products 8082, cart 8083, checkout 8086,
  login 8085) directly.
- Backend is Spring Boot 2.6.3 / Java 17 over YugabyteDB (YCQL + YSQL).
- Critical paths to model: product catalog reads, add-to-cart (YSQL), checkout/order
  (YCQL), and the new `GET /inventory/{asin}` scarcity endpoint.

## SLAs to Assert (from performance-baselines.md / rules.md)
- p95 < 500 ms (baseline 200 ms); p99 < 1000 ms.
- Success rate > 99.0%; error rate < 0.1% at peak.
- No > 10% regression vs baseline.

## Structure (when introduced)
- Extend `Simulation`; define `httpProtocol` (baseUrl = gateway, headers,
  connection); one `scenario()` per user journey; always include `assertions()`.
- Use ramp-up profiles (`rampUsers`, `constantUsersPerSec` for arrival-rate);
  `atOnceUsers()` only for smoke. Include think-time pauses; match production shape.
- Feeders for all parameterized data (CSV/JSON/JDBC) in
  `src/test/resources/feeders/`; `circular`/`random` strategies.

## Required Assertions (drive CI exit code)
```scala
setUp(scn.inject(rampUsers(50).during(30.seconds)))
  .protocols(httpProtocol)
  .assertions(
    global.responseTime.percentile3.lt(500),   // p95 < 500ms (QE-Core)
    global.successfulRequests.percent.gt(99.0)
  )
```

## Reporting & CI
- Review the HTML report each run; focus on p95/p99 (not averages); compare to
  baseline; archive as a CI artifact.
- Maven/Gradle Gatling plugin: smoke sims on every PR, full load pre-release; parse
  the assertion exit code for pass/fail.

## Suggested Dependencies (when introduced)
- `io.gatling:gatling-charts-highcharts`, `io.gatling:gatling-maven-plugin`.
