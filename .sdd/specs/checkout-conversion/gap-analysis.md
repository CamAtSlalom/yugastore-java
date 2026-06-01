# Gap Analysis: checkout-conversion (brownfield)

> Implementation-gap analysis comparing the generated requirements against the
> existing Yugastore codebase. Produced 2026-06-01 against `ai-harness-setup` after
> merging upstream `origin/master` (incl. the YCQL startup fix `b3396a6`).
> Requirements are generated but **not yet approved**; this analysis may inform
> revisions and feeds the design phase.

## 1. Current-state facts that shape everything

- **Checkout call chain (UI → server):** React `Cart.submitCheckout()` → react-ui Spring
  `/cart/checkout` (`CronosProductsController:47`) → `DashboardRestConsumer.checkout()`
  (`react-ui/.../rest/DashboardRestConsumer.java:117`) → gateway
  `/api/v1/shoppingCart/checkout` (`ShoppingCartController:70`) → Feign
  `CheckoutRestClient.checkout()` → checkout `/shoppingCart/checkout`
  (`CheckoutController:24`) → `CheckoutServiceImpl.checkout(userId)`.
- The chain carries **no body and no `requestId`** end-to-end. `userId` is hardcoded
  `"u1001"` at gateway (`ShoppingCartController:36,47,60`) and checkout
  (`CheckoutController:25`); order `user_id` hardcoded to `1` (`CheckoutServiceImpl:103`).
  Confirmed deferred per PO.
- **`CheckoutStatus`** (`checkout/.../domain/CheckoutStatus.java`) is a POJO with
  `status` (String "SUCCESS"/"FAILURE"), `orderNumber`, `orderDetails`. **No
  machine-readable failure-reason field.** The controller catches
  `NotEnoughProductsInStockException` and sets only `status=FAILURE`, dropping the
  message (`:40-44`). Identical `CheckoutStatus` POJOs are **duplicated** in the
  gateway and products modules.
- **YCQL configs** (freshly updated from upstream) are consistent across products
  (`YugabyteYCQLConfig.java`) and checkout (`YugabyteLocalConfig.java`): both build
  `CqlSession` directly with `METADATA_SCHEMA_ENABLED=false`, register a
  `CassandraTemplate` bean, are `@Profile("local")`, and both app classes exclude all
  Cassandra auto-config. `CheckoutServiceImpl` uses the injected `CassandraOperations`
  and `getCqlOperations().execute(String)`.
- **Schema** (`resources/schema.cql`): `product_inventory(asin PK, quantity int)` and
  `orders(order_id PK, user_id text, ...)` are both `transactions = {'enabled':'true'}`.
  `orders.user_id` is **text** (entity maps `Integer` — mismatch). `products` has
  `num_reviews int`, `num_stars int`, `avg_stars double`; entity maps `num_stars` as
  `Double`.
- **No Caffeine, no `@EnableCaching`, no JaCoCo, no feature-flag library** anywhere in
  source or any `pom.xml`. Greenfield for all four.
- **Tests:** only `contextLoads()` `@SpringBootTest` stubs in checkout and products
  (products test under wrong package `com.example.demo`). **No frontend tests at all**;
  Jest runs via `react-scripts 1.1.1 test`.
- **Security:** gateway `SecurityConfiguration` is `permitAll()` + CSRF disabled. No
  input validation at the gateway. `hibernate-validator 7.0.1` is on the classpath but
  unused.

## 2. Per-requirement verdict and mapping

| Req | Verdict | Where |
|-----|---------|-------|
| **1 — Checkout result fidelity** | **EXTEND** (UI) + **EXTEND** (checkout svc) | `Cart/index.js:103-106` optimistic success bug; add `failureReason` to `CheckoutStatus` (×3 copies) populated in `CheckoutController` |
| **2 — Double-submit prevention** | **EXTEND** (UI) + **NEW** (server idempotency) | UI in-flight state + click guard; thread new `requestId` through 6 layers (all currently pass no body); new `(userId,requestId)→result` store on a txn-enabled YCQL table |
| **3 — Review prominence + badge** | **EXTEND** (UI only) | Data already flows (`avg_stars`,`num_reviews`); badge on `avg_stars>=4.5 && num_reviews>=50`; add to detail (`ShowProduct:88-100`), cards, and cart line items (`Cart:64-81`, none today); fix shared `stars[]` bug (`:126-141`) + fetch-in-render (`:50`); new lightweight OSS toggle |
| **4 — Atomic idempotent decrement** | **EXTEND/REWRITE** (checkout svc) | `CheckoutServiceImpl.checkout():47-86` string-built `BEGIN TRANSACTION` → replace with bound LWT `UPDATE product_inventory SET quantity=quantity-:n WHERE asin=:asin IF quantity>=:n`; act on `[applied]`; collapse 2N fan-out (`:57-69` + `getTotal():88-97`); remove misleading `@Transactional`; fix `@Scope(SESSION)` mutable fields; compensation for order-insert failure (no real multi-row ACID) |
| **5 — Scarcity endpoint + cache + toggle** | **NEW** (mostly) | No inventory read endpoint or gateway route/Feign method exists; build `GET /inventory/{asin}` products→gateway→react-ui proxy; bucketed DTO (0=Out,1–5=Few,>5=In); Caffeine `@Cacheable` TTL 5s (new dep); cross-service invalidation is the hard part |
| **6 — Cross-cutting NFRs** | mixed | Coverage/JaCoCo/Jest = **NEW**; perf baseline = **Research**; bound queries = **EXTEND** (via Req 4); gateway validation = **NEW** (`@Validated`, validator already on classpath); stack constraints already satisfied (don't drift) |

## 3. Highest-risk gaps

1. **Req 4 atomic decrement under concurrency (HIGH).** Correct YCQL LWT + order/
   compensation replacing the bogus string "transaction"; needs N-parallel-checkout
   correctness tests that don't exist.
2. **Idempotency `(userId, requestId)` threading + store (Req 2/4).** New param across
   6 layers that pass no body today, plus a new persisted dedup store.
3. **Cross-service cache invalidation (Req 5 AC5).** In-process Caffeine in products
   can't be evicted from checkout; TTL-only is pragmatic but needs sign-off. **Research.**
4. **Zero test/coverage baseline (Req 6).** ≥80%/≥90% gates against `contextLoads()`
   stubs and no frontend tests = net-new harness gating every phase's "done."
5. **`CheckoutStatus` triplication + missing reason field** — drift risk across 3 copies.
6. **`num_stars` ambiguity / schema-entity type mismatch** — badge on `avg_stars` only;
   fix shared-`stars[]` mutation in the same file.

## 4. Recommended sequencing

0. **Phase 0a — Test scaffolding first (Req 6 partial).** JaCoCo on Java modules +
   Jest/RTL setup in `react-ui/frontend`; plan a checkout latency baseline. Without
   this no later phase can prove its coverage gate.
1. **Phase 0b — Req 1 + Req 2 UI.** Read real `CheckoutStatus`, gate success view on
   `status==='SUCCESS'`, add in-flight state/spinner/click-guard; add `failureReason`.
   Highest trust ROI, low risk. Behind a `checkout-trust` canary.
2. **Phase 1 — Req 3 reviews (UI).** Badge + rating/count on detail/cards/cart; fix
   `stars[]` + fetch-in-render; introduce the shared OSS toggle (env-based React flag
   ± config endpoint), reused by `scarcity-message`. No backend change.
3. **Phase 2 — Req 4 + Req 2 server.** Bound LWT decrement; single read pass;
   `(userId,requestId)` idempotency store; compensation; thread `requestId`; remove
   `@Transactional`/session-scoped fields; concurrency tests. Linchpin for Req 5
   accuracy.
4. **Phase 3 — Req 5 scarcity.** `GET /inventory/{asin}` products→gateway→UI proxy;
   bucketed DTO; Caffeine TTL 5s as primary AC5 mechanism (± best-effort evict); UI
   message behind `scarcity-message` toggle.
5. **Phase 4 — Req 6 closeout.** Gateway input validation; final coverage push; perf
   baseline + ≤10% regression check on checkout / `GET /inventory/{asin}` / detail.

**Constraints honored:** Boot 2.6.3 / `javax.*` (Caffeine + flag lib must be 2.6-
compatible OSS); products+checkout YCQL via existing `CassandraTemplate`/`CqlSession`,
cart YSQL — no stack mixing; new external surface only at the gateway; toggles env/
config-based OSS (no A/B platform); QE gates enforced via the Phase 0a harness.

## 5. Research-needed items for design

- (a) YugabyteDB LWT/`[applied]` semantics; batched LWT across inventory+orders vs.
  compensation saga.
- (b) Cross-service Caffeine invalidation vs. TTL-only acceptability for Req 5 AC5.
- (c) Idempotency store schema/TTL and behavior under the still-hardcoded single `userId`.
- (d) Chosen OSS toggle mechanism (CRA env flag vs. small config endpoint vs. Togglz/
  FF4J on the server) and where it is evaluated (SPA vs. gateway).
- (e) Checkout latency baseline numbers before optimization (to measure ≤10% regression).

## Key files

- UI: `react-ui/frontend/src/components/Cart/index.js`, `App/index.js`, `ShowProduct/index.js`
- react-ui proxy: `react-ui/.../controller/CronosProductsController.java`, `.../rest/DashboardRestConsumer.java`
- checkout: `.../cronoscheckoutapi/service/CheckoutServiceImpl.java`, `.../controller/CheckoutController.java`, `.../domain/CheckoutStatus.java` (+ gateway/products duplicates), `.../config/YugabyteLocalConfig.java`
- products: `.../controller/ProductCatalogController.java`, `.../service/impl/ProductInventoryServiceImpl.java`, `.../config/YugabyteYCQLConfig.java`
- gateway: `.../controller/ShoppingCartController.java`, `.../rest/clients/ProductCatalogRestClient.java`
- schema/build: `resources/schema.cql`, `checkout-microservice/pom.xml`, `products-microservice/pom.xml`, root `pom.xml`
