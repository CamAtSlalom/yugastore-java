---
applyTo: "**/*Controller.java,**/*Repository.java,**/*Resource.java,api-gateway-microservice/**"
---

# API Design Standards (Yugastore)

**Tool:** GitHub Copilot · **Source of truth:** `.github/rules/rules.md` (api-design-standards)

- **Single entry point**: external clients call **api-gateway (8081) only**. Inter-service
  routing goes through Eureka service discovery (8761) — never hardcode `host:port` for
  peer services.
- **REST conventions**: Spring Data REST exposes repositories — keep resource paths,
  HAL/`_links`, and pagination contracts consistent. Use proper HTTP verbs and status codes.
  New persistence methods become HTTP endpoints, so be deliberate about `@RepositoryRestResource`
  `path`/`rel` and which methods are exposed.
- **Backward compatibility**: validate API contracts for backward compatibility before
  release. Prefer additive change; deprecate before removing.
- **Validation**: validate request bodies/params; return a consistent error response shape
  across services.
- **Per-service data semantics**: products/checkout = YCQL (Spring Data Cassandra),
  cart/login = YSQL (Spring Data JPA). Respect each store's consistency and key model in
  endpoint design.
- **Documentation**: document public endpoints with rationale, examples, and known limitations.

For contract validation, invoke the `api-validator` agent (`.github/agents/api-validator.md`).
