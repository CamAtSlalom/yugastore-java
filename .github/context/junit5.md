---
name: junit5-project-context
type: context
priority: high
tool: junit5
---

# Context: JUnit 5 Testing Environment (Yugastore)

## Summary
JUnit 5 (JUnit Jupiter) is the unit/slice testing framework for the Yugastore Java microservices. Every Spring Boot module already pulls in JUnit Jupiter, AssertJ, Mockito, and Spring Test transitively through `spring-boot-starter-test`. This context describes how to **validate and extend** that existing setup — no new dependencies or build changes are required.

## Project Profile
- Maven multi-module monorepo, build with `./mvnw`. Java 17, Spring Boot 2.6.3, Spring Cloud 2021.0.0.
- Base package: `com.yugabyte.app.yugastore`.
- Microservices: `eureka-server-local` (8761), `api-gateway` (8081), `products` (8082, YCQL / Spring Data Cassandra), `cart` (8083, YSQL / Spring Data JPA), `checkout` (8086, YCQL), `login` (8085, YSQL).
- Data: YugabyteDB YCQL + YSQL via Spring Data REST.

## When to Use JUnit 5 Here
- Service-layer unit tests with Mockito-mocked repositories/clients.
- Controller slice tests (`@WebMvcTest` + `MockMvc`) for REST endpoints behind the api-gateway.
- Repository slice tests (`@DataJpaTest`) for the JPA-backed `cart` and `login` services.
- Minimal `@SpringBootTest` wiring checks where a full context is genuinely needed.

## Existing Setup (already present — do NOT modify pom.xml)
Each module inherits this from the Spring Boot parent:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
`spring-boot-starter-test` bundles: JUnit Jupiter, AssertJ, Mockito, Hamcrest, JSONassert, Spring Test + Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `MockMvc`, `@MockBean`).

### Common Commands
```bash
# From repo root (multi-module)
./mvnw test                                   # Run all module tests
./mvnw -pl cart test                          # Test only the cart service
./mvnw -pl products test -Dtest=ProductControllerTest   # Single class
./mvnw verify                                 # Tests + verify lifecycle
```

## Test Slice Strategy per Service
| Service | DB | Recommended slices |
|---------|----|--------------------|
| products | YCQL / Cassandra | Mock repository in service tests; `@WebMvcTest` for controllers |
| checkout | YCQL / Cassandra | Mock repository in service tests; `@WebMvcTest` for controllers |
| cart | YSQL / JPA | `@DataJpaTest` for repositories; `@WebMvcTest` for controllers |
| login | YSQL / JPA | `@DataJpaTest` for repositories; `@WebMvcTest` for controllers |
| api-gateway | n/a | `@WebMvcTest` / lightweight routing tests |

## Coverage Targets (JaCoCo — when configured)
| Metric | Minimum | Target |
|--------|---------|--------|
| Line Coverage | 80% | 90% |
| Branch Coverage | 75% | 85% |
| Class Coverage | 90% | 95% |
| Method Coverage | 80% | 90% |

## Available Test Libraries (via spring-boot-starter-test)
- **JUnit Jupiter** — core framework (JUnit 5).
- **AssertJ** — preferred fluent assertions.
- **Mockito** — mocking with the JUnit 5 extension (`@ExtendWith(MockitoExtension.class)`).
- **Spring Boot Test** — `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `MockMvc`, `@MockBean`.
- **JSONassert / JsonPath** — assert on REST JSON payloads.

## IDE Integration
- **IntelliJ IDEA**: built-in JUnit 5 runner with gutter run/debug icons.
- **VS Code**: Java Test Runner extension with JUnit 5 support.
