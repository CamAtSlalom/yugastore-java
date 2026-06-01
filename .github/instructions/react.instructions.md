---
applyTo: "react-ui/frontend/src/**"
---

# React UI Instructions (Yugastore)

**Tool:** GitHub Copilot · **Scope:** `react-ui/frontend` — React 16, Create React App (`react-scripts`), npm

This is an **existing React 16 / CRA** app. Match what's already there — do not migrate
tooling (no Vite/Next), do not introduce TypeScript or a new state library unless asked.

- **Components**: follow the existing component style in the codebase. Prefer functional
  components with hooks for new code; keep one component per file.
- **State**: local state first, lift up when shared. Use Context only for truly global state.
- **API access**: the UI talks **only** to api-gateway (8081) — never call other services
  directly. Keep fetch/axios calls in their existing service/util modules.
- **Keys**: stable, unique `key` props for lists (not array index).
- **Effects**: include all dependencies in `useEffect`; clean up subscriptions.
- **Performance**: don't over-memoize; `React.memo`/`useMemo`/`useCallback` only for
  measured hot paths.
- **Tests**: Jest via `react-scripts test` with `@testing-library/react`; query by role →
  label → text → testId. See `.github/instructions/testing.instructions.md`.
- **Commands**: `npm start` (dev), `npm test`, `npm run build` — all from `react-ui/frontend`.
