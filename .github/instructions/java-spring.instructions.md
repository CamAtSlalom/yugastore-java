---
applyTo: "**/*.java"
---

# Java / Spring (Yugastore)

<!-- Rule Metadata -->
**Tool:** GitHub Copilot
**Version:** 2026-06
**Category:** Technology-Specific Rules
**Scope:** Spring Boot microservices in this repo (`*-microservice`, `eureka-server-local`)

## Purpose

Capture the conventions actually used in Yugastore's Spring Boot microservices so
generated code matches the existing codebase: Java 17, Spring Boot 2.6.3, Spring
Cloud 2021.0.0, Maven multi-module, Eureka discovery, and YugabyteDB accessed via
**two** Spring Data stacks — Cassandra (YCQL) and JPA (YSQL).

## Stack Facts (do not drift from these)

- **Java 17**, **Spring Boot 2.6.3**, **Spring Cloud 2021.0.0**.
- Boot 2.6 ⇒ **`javax.*`** APIs (`javax.persistence`, `javax.servlet`) — **NOT**
  `jakarta.*`. Do not "upgrade" imports to jakarta; it breaks the build.
- Build is Maven via the wrapper: **`./mvnw`** (do not assume a global `mvn`).
- Root `pom.xml` is `<packaging>pom</packaging>` aggregating the modules; each
  microservice inherits from it. Add cross-cutting deps/versions at the root.

## Module & Package Layout

- One Maven module per microservice; module name matches the service
  (`products-microservice`, `cart-microservice`, …).
- Base package: `com.yugabyte.app.yugastore[.<service>]`.
- Sub-packages by responsibility, mirror the existing services:
  `config/`, `controller/`, `service/`, `repo/` (or `repositories/`),
  `domain/`, `exception/`, `util/`, `security/`.
- The `@SpringBootApplication` entry class lives at the service base package root
  (e.g. `YugastoreProducts`, `YugastoreCart`).

## Service Discovery & Configuration

- Every service registers with **Eureka** (`eureka-server-local`, port 8761). The
  registration name comes from `spring.application.name` in `application.yml`.
- Config lives in `src/main/resources/application.yml`; the `local` profile is the
  default active profile. Keep service ports as assigned (products 8082, cart 8083,
  checkout 8086, login 8085, api-gateway 8081, ui 8080).
- Externalize connection settings with `@Value("${...:default}")` and a custom
  property namespace (existing services use `cronos.yugabyte.*`). Always provide a
  sensible default in the `@Value` expression.
- The UI talks **only** to `api-gateway-microservice`; inter-service calls go
  through discovery, not hardcoded hosts.

## Data Access — pick the stack by service

YugabyteDB is reached through whichever Spring Data stack the service already uses.
**Match the service you are editing — never mix the two in one module.**

### YCQL services (products, checkout) → Spring Data Cassandra

```java
@RepositoryRestResource(path = "product")
public interface ProductMetadataRepo extends CassandraRepository<ProductMetadata, String> {

    @Query("SELECT * FROM cronos.products limit ?0 offset ?1")
    @RestResource(path = "products", rel = "products")
    List<ProductMetadata> getProducts(@Param("limit") int limit, @Param("offset") int offset);

    Optional<ProductMetadata> findById(String id);
}
```

- Repositories extend `CassandraRepository<T, IdType>`.
- Configuration extends `AbstractCassandraConfiguration` with
  `@EnableCassandraRepositories(basePackages = …)`, gated by `@Profile("local")`.
  Existing config uses `SchemaAction.CREATE_IF_NOT_EXISTS` and the DataStax
  `CqlSession` driver.
- `@Query` strings are **CQL** (note keyspace-qualified tables like `cronos.products`,
  and CQL `limit/offset`). Positional params are `?0`, `?1`.

### YSQL services (cart, login) → Spring Data JPA

```java
@Entity(name = "shopping_cart")
@Table(name = "shopping_cart")
public class ShoppingCart {
    @Id
    @Column(name = "cart_key")
    private String cartKey;
    @Column(name = "user_id")
    private String userId;
    // getters/setters …
}
```

```java
@RepositoryRestResource
public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, String> {

    @Modifying
    @Transactional
    @Query("UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ?1 AND asin = ?2")
    int updateQuantityForShoppingCart(String userId, String asin);
}
```

- Entities use `javax.persistence` annotations (`@Entity`, `@Table`, `@Id`,
  `@Column`). Column names are explicit snake_case matching `resources/schema.sql`.
- Repositories extend `CrudRepository<T, IdType>`.
- Mutating `@Query` methods need `@Modifying` **and** `@Transactional`.
- Schemas are managed by hand in `resources/schema.sql` (YSQL) and
  `resources/schema.cql` (YCQL) — keep entity/table definitions in sync with them.

### Spring Data REST

Repositories are exposed as REST resources via `@RepositoryRestResource` (and
`@RestResource` on query methods). New persistence methods become HTTP endpoints —
be deliberate about `path`/`rel` and about which methods are exposed.

## Controllers & Services

- Thin `@RestController`s under `controller/`; business logic in `@Service` classes
  under `service/` (interface + `*Impl` is the existing pattern, e.g.
  `ShoppingCart` / `ShoppingCartImpl`).
- Constructor injection over field injection for new code.
- Throw domain exceptions from `exception/` (e.g.
  `NotEnoughProductsInStockException`) rather than returning error sentinels.

## Testing

- Use `spring-boot-starter-test` (JUnit 5 + Spring Test). Test classes live under
  `src/test/java/...` mirroring the package (e.g. `YugastoreCartTests`).
- For data-layer tests, prefer slice/integration tests against the appropriate
  Yugabyte API; do not introduce a second persistence stack just for tests.
- Run a single module's tests with `./mvnw -pl <module> test`; build everything with
  `./mvnw -DskipTests package`.

## Customization Notes

Teams may want to adjust: the `cronos.yugabyte.*` property namespace, Spring Data
REST exposure (some teams prefer explicit `@RestController` endpoints), and the
service/`*Impl` interface split. Confirm before changing cross-module concerns in
the root `pom.xml`.
