---
name: api-standards
type: context
priority: high
tool: claude
loading: on-demand
used-by: [apiValidator, documentationAssistant]
---

# Context: API Design Standards (Yugastore-Java)

# Summary
Conventions for HTTP/REST endpoints across the Spring Boot microservices and the
api-gateway. Derived from `.claude/rules/rules.md` (api-design-standards). Scope:
`**/*Controller.java`, `**/*Repository.java`, `**/*Resource.java`, and the
api-gateway module.

## Single Entry Point
- External clients call **api-gateway (8081) only**. The React UI talks to nothing
  else. Do not introduce new external entry points or expose other services directly.
- Inter-service routing goes through **Eureka discovery (8761)** — never hardcode
  `host:port` for peer services (Feign clients + service IDs).

## REST Conventions
- Spring Data REST exposes repositories (`@RepositoryRestResource`,
  `@RestResource`). Keep resource paths, HAL `_links`, and pagination contracts
  consistent. New persistence methods become HTTP endpoints — be deliberate about
  `path`/`rel` and which methods are exposed (over-exposure is a security finding).
- Use proper HTTP verbs and status codes. Paginate Spring Data REST collections;
  never return unbounded result sets.

## Per-Service Data Semantics
- products (8082) / checkout (8086) = YCQL (Spring Data Cassandra).
- cart (8083) / login (8085) = YSQL (Spring Data JPA).
- Respect each store's consistency and key model in endpoint design. Don't mix the
  two stacks in one module.

## Backward Compatibility
- API contracts are validated for backward compatibility before release (QE-Core
  automated gate). Prefer additive change; deprecate before removing.
- For the checkout-conversion work: the new `GET /inventory/{asin}` (products →
  api-gateway) must return a **bucketed** availability level
  ("Only a few left" / "In stock" / "Out of stock"), not exact counts, and must be
  additive (no change to existing product/cart/checkout contracts).

## Validation & Errors
- Validate request bodies/params; return a consistent error-response shape across
  services. Surface real failures (e.g. out-of-stock at checkout) rather than
  optimistic success.

## Documentation
- Document public endpoints with design rationale, runnable examples (correct
  ports/services, `./mvnw`/`npm`), and known limitations/edge cases.
