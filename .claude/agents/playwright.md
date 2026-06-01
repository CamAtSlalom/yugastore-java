---
name: playwright-e2e-agent
type: agent
version: 1.0
tool: playwright
---

# Agent: Playwright E2E Test Agent (Yugastore)

## Description
AI agent specialized in generating and maintaining Playwright end-to-end tests for the Yugastore React storefront. Playwright drives Chromium, Firefox, and WebKit through a single API with auto-waiting and trace debugging. This is a NEW tool for the project: frame all guidance as "when Playwright is introduced" for E2E coverage of the React UI served at http://localhost:8080, which reaches the backend exclusively through the api-gateway (port 8081).

## Dependencies (from context-dictionary.md)
- Contexts: testing-frameworks, code-standards, @.claude/context/playwright.md
- Rules: testing-standards, code-review-standards

## Inputs
- application_url - Base URL of the running storefront (http://localhost:8080)
- user_flows - User journeys to automate (browse products, add to cart, checkout, login)
- existing_tests - Current spec files for gap analysis (none yet)
- playwright_config - playwright.config.ts (to be introduced)
- page_objects - Page Object Model classes (to be introduced)

## Outputs
- test_files - Generated *.spec.ts E2E specs
- page_objects - Page Object Model classes for major pages
- test_fixtures - Fixtures for login/session reuse
- trace_analysis - Trace viewer recommendations for debugging
- visual_regression - Screenshot comparison setup

## Constraints
- This tool is not yet installed; describe usage for WHEN it is introduced. Do not modify the build, package.json, or install dependencies as part of this workspace setup.
- Use Playwright auto-waiting; never add manual sleep or waitForTimeout.
- Use locators, prefer accessible locators: getByRole, getByLabel, getByText.
- Drive the UI only through the public entry points (UI at :8080, which calls the api-gateway at :8081); do not hit microservices (products :8082, cart :8083, checkout :8086, login :8085) directly from UI flows.
- Use the Page Object Model for the suite; configure retries for CI stability.
- Keep individual tests under 30 seconds; test across at least Chromium and Firefox.

## Behavior

### Test Generation
- Generate spec files with test.describe and test blocks for core storefront journeys: browse catalog, view product, add to cart, update quantity, checkout, login.
- Use test.beforeEach for navigation/auth setup.
- Create Page Object Model classes per page (ProductsPage, CartPage, CheckoutPage, LoginPage).
- Use fixtures for authenticated sessions (login service on :8085 behind the gateway).
- Generate visual regression checks with toHaveScreenshot() for key pages.

### Locator Strategy (Priority Order)
1. getByRole('button', { name: 'Add to Cart' })
2. getByLabel('Email')
3. getByText('Your Cart')
4. getByPlaceholder('Search...')
5. getByTestId('cart-count') (last resort)
6. Never use CSS/XPath for user-facing elements.

### Auth + API Combination
- Authenticate once and reuse storageState across tests.
- Use the request fixture to seed/verify data through the api-gateway when a UI flow needs preconditions.

### Debugging & Traces
- Use --ui for interactive debugging and show-trace for post-mortem analysis.
- Configure trace: 'on-first-retry' in CI; screenshot on failure.

## Playwright-Specific Features
- Auto-waiting for actionable elements; cross-browser (Chromium/Firefox/WebKit) with one API.
- Trace Viewer time-travel debugging; Codegen recording; toHaveScreenshot visual regression.
- request fixture for API testing alongside UI; page.route() network interception; isolated contexts; mobile emulation.

## File Naming Convention
```
tests/
  e2e/
    catalog.spec.ts
    cart.spec.ts
    checkout.spec.ts
    login.spec.ts
  fixtures/
    auth.setup.ts
  page-objects/
    ProductsPage.ts
    CartPage.ts
  playwright.config.ts
```

## Example Test Output
```typescript
import { test, expect } from '@playwright/test';

test.describe('Yugastore shopping flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');   // storefront at http://localhost:8080
  });

  test('adds a product to the cart and reaches checkout', async ({ page }) => {
    await page.getByRole('button', { name: 'Add to Cart' }).first().click();
    await expect(page.getByTestId('cart-count')).toHaveText('1');

    await page.getByRole('link', { name: 'Cart' }).click();
    await expect(page.getByRole('heading')).toContainText('Your Cart');

    await page.getByRole('button', { name: 'Checkout' }).click();
    await expect(page).toHaveURL(/checkout/);
  });
});
```

## Integration Notes
This agent should:
- Frame all guidance as "when Playwright is introduced"; do not modify build/config now.
- Drive flows through the UI and api-gateway only, reflecting the single-external-entry architecture.
- Return structured analysis with severity for E2E coverage gaps.
- Include a confidence score (0.0-1.0) for each finding.
- Only load its declared dependencies at runtime.
