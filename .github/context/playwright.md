---
name: playwright-project-context
type: context
priority: high
tool: playwright
---

# Context: Playwright Testing Environment (Yugastore)

## Summary
Playwright is the proposed end-to-end testing framework for the Yugastore React storefront. It drives Chromium, Firefox, and WebKit through one API with auto-waiting, trace debugging, codegen, and built-in visual comparison. Playwright is **not yet present** in this repo — this context describes how it should be used **when introduced**. It does not authorize installing dependencies or changing the build during workspace setup.

## Project Profile
- Storefront UI served at `http://localhost:8080` (react-ui/frontend, React 16 / CRA).
- The UI calls the backend exclusively through the `api-gateway` (port 8081) — the sole external entry point.
- Backend microservices behind the gateway: products (8082, YCQL), cart (8083, YSQL), checkout (8086, YCQL), login (8085, YSQL), eureka-server-local (8761).

## When to Use Playwright Here
- Cross-browser E2E of full storefront journeys: browse catalog, product detail, add/update cart, login, checkout.
- Visual regression on key pages (catalog, cart, checkout).
- Combined API + UI tests: seed/verify state through the api-gateway, then assert in the browser.

## Key Configuration (when introduced)

### playwright.config.ts
```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/e2e',
  timeout: 30000,
  retries: process.env.CI ? 2 : 0,
  reporter: [['html'], ['junit', { outputFile: 'results.xml' }]],
  use: {
    baseURL: 'http://localhost:8080',     // Yugastore storefront
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
  ],
});
```

### Common Commands
```bash
npx playwright test                       # Run all E2E tests
npx playwright test --ui                  # Interactive UI mode
npx playwright test --project=chromium    # Single browser
npx playwright show-report                # View HTML report
npx playwright show-trace trace.zip       # Inspect a trace
npx playwright codegen http://localhost:8080   # Record against the storefront
npx playwright install                    # Install browsers
```

## Architecture Notes for E2E
- Drive flows through the UI at :8080 and let it call the api-gateway at :8081 — mirror how real users interact.
- Use the `request` fixture against the gateway for preconditions (seed products, create a login), never against individual microservice ports.
- The login service (:8085 behind the gateway) backs authenticated journeys; capture storageState once and reuse.

## Suggested Dependencies (when introduced)
```json
{
  "devDependencies": {
    "@playwright/test": "^1.42.x",
    "@axe-core/playwright": "^4.x"
  }
}
```

## Ecosystem
- **@playwright/test** — runner with fixtures, assertions, parallelism.
- **@axe-core/playwright** — accessibility checks during E2E.
- **Trace Viewer / Codegen** — debugging and test recording.
- **Playwright MCP** — Model Context Protocol server for AI-driven browser automation.
