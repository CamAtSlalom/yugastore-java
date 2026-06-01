---
name: jest-project-context
type: context
priority: high
tool: jest
---

# Context: Jest Testing Environment (Yugastore react-ui)

## Summary
Jest is the JavaScript testing framework for the Yugastore React storefront located at `react-ui/frontend`. The app is React 16 built with Create React App (react-scripts 1.1.1), so Jest is already wired in through react-scripts and run with `npm test`. This context describes how to **validate and extend** that existing setup — no eject, dependency, or config changes are required.

## Project Profile
- UI module: `react-ui/frontend` — React 16, Create React App (react-scripts 1.1.1), npm.
- Talks to the backend only through the `api-gateway` (port 8081), the sole external entry point.
- Test runner: Jest, provided and configured by react-scripts (no standalone jest.config needed).

## When to Use Jest Here
- Unit/component tests for storefront components (product catalog, cart, checkout, login).
- Snapshot tests for stable render output.
- Reducer/container logic tests for any redux/state code.
- Mocked-network tests that isolate the UI from the live api-gateway.

## Existing Setup (already present — do NOT eject or modify)
react-scripts ships and configures Jest. Tests run via the package script:
```json
{
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test --env=jsdom",
    "eject": "react-scripts eject"
  }
}
```

### Common Commands
```bash
# From react-ui/frontend
npm test                       # Jest in watch mode (CRA default)
CI=true npm test               # Single run, suitable for CI
CI=true npm test -- --coverage # Generate coverage report
npm test -- ProductList        # Run tests matching a name/path
```

## Conventions for This Repo
- CRA 1.1.1 auto-discovers `*.test.js` and `*.spec.js` files and files under `__tests__/`.
- Co-locate test files next to the component they cover.
- Mock calls to the api-gateway (port 8081) with `jest.mock()` so component tests stay fast and isolated.
- Keep snapshots small and meaningfully named so diffs are reviewable.
- Do NOT introduce TypeScript/ts-jest, swap the runner, or eject — work within react-scripts.

## Coverage Targets
| Metric | Minimum | Target |
|--------|---------|--------|
| Statements | 80% | 90% |
| Branches | 80% | 85% |
| Functions | 80% | 90% |
| Lines | 80% | 90% |

## Available Tooling (via react-scripts)
- **Jest** — runner, assertions, mocking, snapshots, coverage (Istanbul) — all bundled.
- **react-test-renderer** — render components to JSON for snapshot tests (CRA-compatible).
- Note: react-scripts 1.1.1 predates `@testing-library/react`; respect whatever rendering helper the repo already uses and do not add new libraries as part of validate-and-extend.

## CI/CD Notes
- Use `CI=true` to force a single non-watch run.
- Coverage output lands in `coverage/` (add to `.gitignore`).
- There is no CI/CD pipeline yet; tests are run locally with `npm test`.
