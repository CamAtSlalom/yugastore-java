# Requirements Document

## Project Description (Input)

Checkout conversion improvements for Yugastore. Goal: reduce cart abandonment and raise checkout completion. Scope (near-term; A/B testing and the "people viewing" social-proof counter are explicitly deferred):

- **Phase 0 (trust/reliability):** Fix the checkout UX so the React Cart reads the real `CheckoutStatus` (SUCCESS/FAILURE) instead of optimistically showing "Thank you!" on click; disable the checkout button and show a loading state during the call to prevent double-submit; surface real failures (especially out-of-stock) to the shopper.
- **Phase 1 (reviews highlight):** Add a "Highly recommended" badge and greater review prominence using existing product fields (`avg_stars`, `num_reviews`) on product detail, product cards, and cart line items; behind a simple on/off feature toggle.
- **Phase 2 (inventory correctness):** Make checkout inventory decrement atomic and safe in YugabyteDB YCQL using a conditional update (`UPDATE product_inventory SET quantity = quantity - n WHERE asin = ? IF quantity >= n`) with bound/prepared statements (no string concatenation), idempotency keyed by `(userId, requestId)`, and collapse the duplicate 2N product-detail fan-out into a single read pass. Tables `product_inventory` and `orders` are already transactions-enabled.
- **Phase 3 (scarcity display):** Expose a new read endpoint `GET /inventory/{asin}` (products-microservice → api-gateway), returning a bucketed availability level ("Only a few left" / "In stock" / "Out of stock") rather than exact counts, with a short-TTL cache and cache invalidation on checkout decrement; render the scarcity message on product detail and cart behind a feature toggle.

**Constraints:** Spring Boot 2.6.3 / Java 17 / `javax.*` (not `jakarta.*`), Spring Cloud 2021.0.0, Eureka discovery, React 16 UI talking only to api-gateway (8081). Feature flags/canary must use open-source tooling only; full A/B experimentation is out of scope. All external traffic flows through api-gateway. Follow the QE quality gates in `.claude/rules/rules.md` (coverage ≥80% new code, critical paths ≥90%, API p95 < 500ms).

**Reference:** `docs/CART_CHECKOUT_UX_ANALYSIS.md` (full friction analysis and file:line evidence); `docs/SOLUTION_ARCHITECTURE_REPORT.md` (system architecture).

## Introduction

This document specifies requirements for the **checkout-conversion** feature set in Yugastore. The goal is to reduce cart abandonment and raise checkout completion by first restoring trust in the checkout flow, then layering credibility (reviews) and urgency (scarcity) signals on top of a corrected, oversell-safe inventory path.

Requirements are organized into the four delivery phases from `docs/CART_CHECKOUT_UX_ANALYSIS.md` (Phase 0–3) plus a cross-cutting non-functional group. Each requirement is traceable to a phase and to the friction findings (F1–F14) and architecture risks (C1–C4, H1–H3) cited in `docs/CART_CHECKOUT_UX_ANALYSIS.md` and `docs/SOLUTION_ARCHITECTURE_REPORT.md`.

**Subjects used in EARS criteria:** `Checkout UI` (React `react-ui`), `Checkout Service` (checkout-microservice, 8086, YCQL), `Products Service` (products-microservice, 8082, YCQL), `API Gateway` (api-gateway-microservice, 8081). External clients reach the system only through the API Gateway.

### In Scope

- Phase 0 — Checkout result fidelity and double-submit prevention.
- Phase 1 — Review prominence and "Highly recommended" badge behind a feature toggle.
- Phase 2 — Atomic, idempotent inventory decrement and single-pass checkout reads.
- Phase 3 — Bucketed scarcity availability endpoint and display behind a feature toggle.
- Cross-cutting non-functional requirements (quality gates, performance SLAs, security, architecture constraints).

### Out of Scope

- **Full A/B experimentation / measurement platform** (assignment service, stats engine, conversion-event pipeline, dashboards). Feature toggles in this spec are simple open-source on/off flags only.
- **"N people viewing" social-proof counter** — no data source exists; deferred.
- **Exact-count scarcity display** (e.g. "3 left"); only bucketed levels are in scope.
- **Reserve-on-add inventory holds**, keyset/cursor pagination, caching of catalog reads, resilience (timeouts/circuit breakers), and front-end dependency upgrades — noted in the source analysis but not part of this feature.

### Assumptions and Known Dependencies

- **Hardcoded single-user identity (`userId` "u1001", order `user_id=1`) is NOT fixed by this spec** (finding F5/H1). **PO decision (confirmed 2026-06-01): accept the risk and defer** — real per-shopper identity is out of scope here. The idempotency contract in Requirement 4 is keyed on `(userId, requestId)`; it deduplicates retries within the current (single-user) identity and will become fully multi-user-correct only once real identity lands. Documented limitation, not a blocker for this feature.
- `product_inventory` and `orders` are already declared `transactions = {'enabled':'true'}` in `resources/schema.cql`, so atomic/idempotent writes are achievable on the current schema.
- Review fields `avg_stars` (Double) and `num_reviews` (Integer) already exist on `ProductMetadata`; `num_stars` semantics are ambiguous (schema `int` vs entity `Double`) and must not be used as the badge driver — the badge is driven by `avg_stars` only.

### Confirmed Parameters (PO decisions, 2026-06-01)

| Parameter | Confirmed value |
|---|---|
| Identity handling | Accept risk / defer real identity (keep `u1001`) |
| Scarcity cache | **Caffeine in-process** (OSS, no new infra), short TTL **default 5s**, invalidate on decrement |
| "Highly recommended" badge | `avg_stars >= 4.5` **AND** `num_reviews >= 50` |
| "Only a few left" bucket | quantity **1–5** = "Only a few left"; **0** = "Out of stock"; **>5** = "In stock" |

## Requirements

### Requirement 1: Checkout Result Fidelity (Phase 0)

**Objective:** As a shopper, I want the checkout page to reflect the actual server result, so that I am never told an order succeeded when it did not.

_Traceability: Phase 0; findings F1, F3; bug `react-ui Cart/index.js:103-106` (sets `isCompleted=true` on click regardless of result)._

#### Acceptance Criteria

1. When the shopper clicks the checkout button, the Checkout UI shall submit the checkout request and wait for the server response before changing the checkout view.
2. When the Checkout Service returns `CheckoutStatus.SUCCESS`, the Checkout UI shall display the order confirmation (success) view.
3. If the Checkout Service returns `CheckoutStatus.FAILURE`, then the Checkout UI shall keep the shopper on the cart and display a failure message, and shall not display any order confirmation.
4. If the checkout failure cause is insufficient inventory (out of stock), then the Checkout UI shall display a clear out-of-stock message identifying that an item is unavailable.
5. If the checkout request fails due to a network or server error (no `CheckoutStatus` received), then the Checkout UI shall display a retryable error message and shall not display order confirmation.
6. The Checkout UI shall determine the success or failure view solely from the `CheckoutStatus` returned by the Checkout Service, and shall not infer success from the click event.
7. The Checkout Service shall return a `CheckoutStatus` whose result and failure reason (e.g. out-of-stock) are machine-readable by the Checkout UI.

### Requirement 2: Double-Submit Prevention (Phase 0)

**Objective:** As a shopper, I want the checkout action to be safe to click once, so that I do not create duplicate orders or double-charge/double-decrement.

_Traceability: Phase 0; finding F2; bug `react-ui Cart/index.js:103-107` (button disabled only when cart is empty)._

#### Acceptance Criteria

1. When the shopper clicks the checkout button, the Checkout UI shall disable the checkout button before the request is sent.
2. While a checkout request is in flight, the Checkout UI shall display a loading indicator and shall ignore additional clicks on the checkout button.
3. When the checkout request completes (success or failure), the Checkout UI shall remove the loading indicator and re-enable the checkout button only if the cart is still actionable.
4. If a duplicate or retried checkout request is received for the same `(userId, requestId)`, then the Checkout Service shall return the result of the original request and shall not create a second order.
5. If a duplicate or retried checkout request is received for the same `(userId, requestId)`, then the Checkout Service shall not apply a second inventory decrement.
6. The Checkout UI shall generate a unique `requestId` per checkout attempt and include it on the checkout request so the server can deduplicate retries.

### Requirement 3: Review Prominence and "Highly Recommended" Badge (Phase 1)

**Objective:** As a shopper, I want clear review signals and a recommendation badge on products, so that I trust well-reviewed items and complete purchases.

_Traceability: Phase 1; analysis §4.1; data fields `avg_stars`, `num_reviews` already present (`ProductMetadata.java:48-52`)._

#### Acceptance Criteria

1. Where the `reviews-highlight` toggle is enabled, the Checkout UI shall display the product rating (`avg_stars`) and review count (`num_reviews`) on the product detail page, on product cards, and on cart line items.
2. Where the `reviews-highlight` toggle is enabled and a product satisfies the recommended threshold (`avg_stars >= 4.5` AND `num_reviews >= 50`), the Checkout UI shall display a "Highly recommended" badge on that product.
3. Where the `reviews-highlight` toggle is disabled, the Checkout UI shall not display the "Highly recommended" badge and shall preserve the existing review presentation.
4. If a product has no reviews (`num_reviews` is zero or absent), then the Checkout UI shall not display the "Highly recommended" badge for that product.
5. The Checkout UI shall use `avg_stars` (not `num_stars`) as the rating value driving the badge and star display.
6. The `reviews-highlight` toggle shall be implemented with open-source / lightweight on/off flag tooling and shall not require an A/B experimentation platform.

### Requirement 4: Atomic, Idempotent Inventory Decrement (Phase 2)

**Objective:** As the business, I want checkout to never oversell and never double-decrement, so that orders accepted can actually be fulfilled.

_Traceability: Phase 2; analysis §5; risks C1, C2, C3, H2; `CheckoutServiceImpl.java:50-79`._

#### Acceptance Criteria

1. When a checkout decrements stock for an item, the Checkout Service shall apply a conditional update guarded by `IF quantity >= n` so that quantity can never go below zero.
2. If the conditional decrement is not applied because available quantity is less than requested, then the Checkout Service shall return `CheckoutStatus.FAILURE` with an out-of-stock reason and shall not record the order as successful.
3. The Checkout Service shall execute all inventory and order writes using bound/prepared statements and shall not construct any query by string concatenation.
4. The Checkout Service shall not execute the invalid `BEGIN TRANSACTION … END TRANSACTION` string; it shall use YugabyteDB's supported transactional/conditional primitives on the transaction-enabled `product_inventory` and `orders` tables.
5. When two or more checkouts for the same low-stock item are processed concurrently, the Checkout Service shall ensure the final quantity is never negative and the number of successful decrements equals the number of successful orders.
6. When a checkout requires product details and inventory for cart items, the Checkout Service shall read each item's data in a single pass and shall not perform the duplicate `getTotal()` fan-out (no 2N downstream calls).
7. If any step of the checkout (decrement, order insert, cart clear) fails after a partial change, then the Checkout Service shall compensate or fail atomically so that order state and inventory remain consistent.
8. The Checkout Service shall enforce idempotency keyed on `(userId, requestId)` for the decrement-and-order operation. _(Note: full per-user correctness assumes real per-shopper identity, which is currently hardcoded — see Assumptions.)_

### Requirement 5: Scarcity Availability Endpoint and Display (Phase 3)

**Objective:** As a shopper, I want a truthful availability signal on products and in my cart, so that I feel urgency without being misled by an exact count.

_Traceability: Phase 3; analysis §4.2; new read path, bucketed output, short-TTL cache with invalidation on decrement._

#### Acceptance Criteria

1. The Products Service shall expose a read endpoint `GET /inventory/{asin}` that returns a bucketed availability level for the given ASIN.
2. The Products Service shall return availability as a bucketed level — "Out of stock" when quantity is 0, "Only a few left" when quantity is 1–5, and "In stock" when quantity is greater than 5 — and shall not return the exact remaining count.
3. The `GET /inventory/{asin}` endpoint shall be reachable by external clients only through the API Gateway (8081) and shall not be exposed as a direct external entry point.
4. When inventory for an ASIN is read, the Products Service shall serve it from a short-TTL in-process cache (Caffeine, default TTL 5 seconds) on cache hit and populate the cache from the datastore on cache miss.
5. When a checkout successfully decrements an ASIN, the system shall invalidate (or write-through) the cached availability for that ASIN so the displayed bucket reflects the change within the TTL bound.
6. If the requested ASIN does not exist or has no inventory record, then the Products Service shall return an "Out of stock" (or not-available) response rather than an error that breaks the page.
7. Where the `scarcity-message` toggle is enabled, the Checkout UI shall render the bucketed availability message on the product detail page and on cart line items.
8. Where the `scarcity-message` toggle is disabled, the Checkout UI shall not display any scarcity message.
9. The `scarcity-message` toggle shall be implemented with open-source / lightweight on/off flag tooling and shall not require an A/B experimentation platform.

### Requirement 6: Cross-Cutting Non-Functional Requirements

**Objective:** As an engineering team, I want every change to meet the project's quality, performance, security, and architecture gates, so that conversion gains do not regress reliability or trust.

_Traceability: `.claude/rules/rules.md` (QE gates, perf SLAs, security, api-design, agent-constraints); architecture constraints from `CLAUDE.md` / `java-spring.md`._

#### Acceptance Criteria

1. The checkout-conversion delivery shall achieve >= 80% coverage on new code, and >= 90% coverage on the critical cart and checkout paths.
2. The system shall keep API latency at p95 < 500 ms and p99 < 1000 ms for the affected endpoints (checkout, `GET /inventory/{asin}`, product detail).
3. The system shall keep the error rate < 0.1% at peak for the affected endpoints.
4. The checkout-conversion delivery shall not introduce a performance regression greater than 10% versus the established baseline.
5. The Checkout Service and Products Service shall use parameterized/bound queries for all data access and shall not concatenate query strings (YCQL and YSQL).
6. The system shall not contain hardcoded secrets, credentials, or database connection strings; configuration shall come from environment/`application` properties.
7. When a request enters the system, the API Gateway shall validate inputs (including the `asin` path parameter and checkout payload) before forwarding to downstream services.
8. The system shall remain on Spring Boot 2.6.3, Java 17, Spring Cloud 2021.0.0, and shall use `javax.*` APIs (not `jakarta.*`).
9. The system shall route inter-service calls through Eureka discovery and shall keep the API Gateway (8081) as the sole external entry point; no new external entry points shall be introduced.
10. The Checkout UI shall be implemented within the existing React 16 `react-ui` module and shall talk only to the API Gateway.
11. All feature toggles introduced by this feature shall use open-source tooling only and shall not introduce a proprietary or A/B experimentation platform.
