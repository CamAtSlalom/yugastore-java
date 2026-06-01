---
name: project-overview
type: context
priority: high
tool: claude
loading: always
---

# Context: Project Overview (Yugastore-Java)

## Summary
Yugastore (Java) — a sample microservices eCommerce marketplace demonstrating
YugabyteDB. Maven multi-module monorepo of Spring Boot services behind an API
gateway, with a React storefront. Java 17, Spring Boot 2.6.3, Spring Cloud
2021.0.0. Boot 2.6 => javax.* namespace (NOT jakarta).

## Tech Stack
- **Backend**: Java 17, Spring Boot 2.6.3, Spring Cloud 2021.0.0, Eureka discovery
- **Build**: Maven multi-module monorepo via ./mvnw (root pom aggregates modules)
- **Frontend**: React 16, Create React App (react-scripts 1.1.1), npm
- **Database**: YugabyteDB — YCQL (Cassandra-compatible) + YSQL (Postgres-compatible)
- **Data load**: Python 3 scripts in resources/ (~6K products)
- **Config**: namespace cronos.yugabyte.*, active profile local

## Microservices & Ports
| Service | Port | Role | Data Layer |
|---------|------|------|-----------|
| eureka-server-local | 8761 | Eureka service discovery | — |
| api-gateway-microservice | 8081 | Sole external entry point (UI talks only here) | — |
| products-microservice | 8082 | Product catalog | YCQL (Spring Data Cassandra) |
| cart-microservice | 8083 | Shopping cart | YSQL (Spring Data JPA) |
| checkout-microservice | 8086 | Checkout / orders | YCQL |
| login-microservice | 8085 | Auth (WIP) | YSQL |

Base package for all services: com.yugabyte.app.yugastore.

## YCQL vs YSQL Split
- **YCQL** (Cassandra API): products, checkout. Schema in resources/schema.cql.
- **YSQL** (Postgres API): cart, login. Schema in resources/schema.sql.
- Repositories exposed via Spring Data REST (@RepositoryRestResource).

## Key Commands
- Build (skip tests): ./mvnw -DskipTests package
- Run a service: ./mvnw spring-boot:run (from the module dir)
- Frontend dev: npm start (in react-ui/frontend)
- Frontend tests: npm test (react-scripts test, Jest/jsdom)
- Java tests: ./mvnw test
- Containerized run: ./docker-run.sh

## Quality Targets (being added)
E2E: Playwright (React UI). Performance: Gatling/JMeter (APIs).
Security: SonarQube (SAST) + Snyk (deps). Unit: validate existing JUnit + Jest.
No CI/CD, E2E, perf, security, or accessibility tooling exists yet.
