---
applyTo: ".sdd/specs/**,.sdd-settings/**"
---

# Spec-Driven Development — Working Inside Specs

**Tool:** GitHub Copilot · **Scope:** `.sdd/specs/**` (feature specs) and `.sdd-settings/**` (methodology + templates)

When editing files under `.sdd/specs/`, you are working within the SDD methodology. Drive
this through the `/sdd-*` prompt files in `.github/prompts/` rather than hand-editing spec
artifacts ad-hoc.

- **Phase separation is strict.** Each spec progresses init → requirements → design → tasks
  → impl → validate. Do **not** generate later-phase artifacts during an earlier phase.
- **Spec structure** per feature: `.sdd/specs/<feature>/` holds `spec.json` (metadata +
  language), `requirements.md`, `design.md`, `tasks.md`.
- **Templates** live in `.sdd-settings/templates/`; methodology rules in `.sdd-settings/rules/`.
  Use template placeholders (`{{FEATURE_NAME}}`, `{{TIMESTAMP}}`, etc.) — don't invent shapes.
- **Requirements** are EARS-format and testable. **Design** must reference real Yugastore
  services and their data stack (YCQL vs YSQL). **Tasks** are small, ordered, TDD-friendly.
- **Implementation**: one task at a time (`/sdd-spec-impl <feature> 1.1`); clear context
  between tasks; keep tests green.
- **Brownfield**: run `/sdd-validate-gap <feature>` after requirements to reconcile against
  existing code before designing.

The agents backing these prompts are in `.github/agents/sdd-*.agent.md`.
