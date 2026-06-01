---
name: context-dictionary
description: |
  Context Dictionary — the central registry for all agents, rules, and context definitions.
  Enables selective context loading so only relevant pieces are loaded at runtime.
  Each AI tool maps this dictionary to its native selective loading mechanism.
version: 1.0
---

# Context Dictionary

This is the **single source of truth** for what context exists, what depends on what, and how each AI tool should load it. The goal is to avoid loading all 27 definitions (9 agents, 8 rules, 10 contexts) at once — instead, load only what's needed for the current task.

---

## How This Works

```
┌─────────────────────────────────────────────────────┐
│  Context Dictionary (this file)                     │
│  ─────────────────────────────────────              │
│  Lightweight index of ALL definitions               │
│  + dependency mapping per agent                     │
│  + loading behavior per tool                        │
└────────┬────────────────────────────────────────────┘
         │
         ▼  Setup agent reads this FIRST (< 200 lines)
┌─────────────────────────────────────────────────────┐
│  Tool-Specific Selective Loading                    │
│  ─────────────────────────────────────              │
│  Claude:  @import + path-scoped .claude/rules/      │
│  Copilot: applyTo: frontmatter in instructions/     │
│  Kiro:    spec-scoped context + steering separation │
└─────────────────────────────────────────────────────┘
```

**Principle**: Load the dictionary index first (< 200 lines). Then load only the detailed definitions each agent actually needs — never all 27 at once.

---

## Agent Registry

Each agent lists **only** the contexts and rules it requires. During setup, the tool generates files that wire these dependencies using native selective loading.

| Agent ID | Summary | Required Contexts | Required Rules | Load Behavior |
|----------|---------|-------------------|----------------|---------------|
| `codeReviewer` | Code review with quality gates | `code-standards`, `quality-metrics` | `code-review-standards`, `security-standards` | On PR/review task |
| `mergeRequest` | PR readiness + gate validation | `ci-cd-pipeline`, `quality-metrics` | `code-review-standards`, `ci-cd-standards` | On PR evaluation |
| `debugger` | Root cause analysis + fixes | `code-standards`, `testing-frameworks` | `testing-standards` | On error/debug task |
| `testGenerator` | Test creation from requirements | `testing-frameworks`, `code-standards` | `testing-standards`, `code-review-standards` | On test gen task |
| `performanceAnalyzer` | Perf bottleneck identification | `performance-baselines`, `code-standards` | `performance-standards` | On perf analysis |
| `securityScanner` | Vulnerability scanning + remediation | `security-policies`, `dependency-mgmt` | `security-standards` | On security scan |
| `documentationAssistant` | Doc generation + maintenance | `code-standards`, `api-standards` | `documentation-standards` | On doc task |
| `cicdValidator` | Pipeline config validation | `ci-cd-pipeline`, `security-policies` | `ci-cd-standards`, `security-standards` | On CI/CD task |
| `apiValidator` | API design + contract validation | `api-standards`, `code-standards` | `api-design-standards`, `code-review-standards` | On API task |

### Always-Loaded (Lightweight Core)

These are loaded every session regardless of task — must be kept small (< 50 lines each):

| Definition | Type | Purpose |
|------------|------|---------|
| `project-overview` | context | Tech stack, key commands, team structure |
| `agent-constraints` | rule | Safety boundaries for all agents |

---

## Context Registry

| Context ID | Summary | Source File | Used By Agents |
|------------|---------|-------------|----------------|
| `project-overview` | Tech stack, goals, team structure | `starter-context.md` | **All** (always-loaded core) |
| `code-standards` | Conventions, linting, style guides | `starter-context.md` | `codeReviewer`, `debugger`, `testGenerator`, `performanceAnalyzer`, `documentationAssistant`, `apiValidator` |
| `quality-metrics` | Coverage targets, quality gates | `starter-context.md` | `codeReviewer`, `mergeRequest` |
| `team-structure` | Roles, review authorities, CODEOWNERS | `starter-context.md` | `mergeRequest` (on demand) |
| `ci-cd-pipeline` | Pipeline arch, workflows, gates | `starter-context.md` | `mergeRequest`, `cicdValidator` |
| `testing-frameworks` | Test tools, organization, conventions | `starter-context.md` | `debugger`, `testGenerator` |
| `security-policies` | Compliance, vulnerability handling | `starter-context.md` | `securityScanner`, `cicdValidator` |
| `performance-baselines` | Benchmarks, SLAs, acceptable metrics | `starter-context.md` | `performanceAnalyzer` |
| `api-standards` | API patterns, versioning, contracts | `starter-context.md` | `documentationAssistant`, `apiValidator` |
| `dependency-mgmt` | Package policies, update strategy | `starter-context.md` | `securityScanner` |

---

## Rules Registry

| Rule ID | Summary | Source File | Applied By Agents |
|---------|---------|-------------|-------------------|
| `code-review-standards` | Code review, testing, quality gates | `starter-rules.md` | `codeReviewer`, `mergeRequest`, `testGenerator`, `apiValidator` |
| `agent-constraints` | AI agent behavioral boundaries | `starter-rules.md` | **All** (always-loaded core) |
| `testing-standards` | Framework selection, coverage, determinism | `starter-rules.md` | `testGenerator`, `debugger` |
| `security-standards` | Vulnerability levels, compliance | `starter-rules.md` | `codeReviewer`, `securityScanner`, `cicdValidator` |
| `performance-standards` | Benchmarks, optimization guidelines | `starter-rules.md` | `performanceAnalyzer` |
| `api-design-standards` | API patterns, versioning, compatibility | `starter-rules.md` | `apiValidator`, `documentationAssistant` |
| `documentation-standards` | Doc format, maintenance protocols | `starter-rules.md` | `documentationAssistant` |
| `ci-cd-standards` | Pipeline requirements, deploy gates | `starter-rules.md` | `cicdValidator`, `mergeRequest` |

---

## Tool-Specific Loading Strategies

### Claude — `@import` + Path-Scoped Rules

```
CLAUDE.md (< 200 lines)
├── @project-overview             ← always imported (lightweight)
├── @.claude/rules/agent-constraints.md  ← always loaded (no paths: frontmatter)
│
├── .claude/rules/                ← modular, path-scoped
│   ├── code-review.md            (paths: src/**) ← only when touching src/
│   ├── security.md               (paths: src/**) ← only when touching src/
│   ├── testing.md                (paths: **/*.test.*, **/*.spec.*) ← only in test files
│   ├── api-design.md             (paths: src/api/**) ← only for API files
│   └── ci-cd.md                  (paths: .github/workflows/**) ← only for CI files
│
├── .claude/agents/               ← on-demand (loaded on invocation only)
│   ├── code-reviewer.md          ← imports: code-standards, quality-metrics
│   ├── test-generator.md         ← imports: testing-frameworks, code-standards
│   ├── security-scanner.md       ← imports: security-policies, dependency-mgmt
│   └── ...
```

**Key**: CLAUDE.md stays under 200 lines using `@import` for lazy references. Path-scoped rules in `.claude/rules/` only load when Claude reads matching files. Agents in `.claude/agents/` load on invocation with their own `@import` dependencies.

### GitHub Copilot — `applyTo:` Frontmatter + AGENTS.md

```
.github/
├── copilot-instructions.md       ← repo-wide core rules (< 500 lines)
│   Contains: project-overview, agent-constraints, code-standards (concise)
│
├── instructions/                 ← path-specific (loaded only for matching files)
│   ├── testing.instructions.md   (applyTo: "**/*.test.*") ← testing-standards
│   ├── api.instructions.md       (applyTo: "src/api/**") ← api-design-standards
│   ├── security.instructions.md  (applyTo: "src/**") ← security-standards
│   ├── cicd.instructions.md      (applyTo: ".github/workflows/**") ← ci-cd-standards
│   └── performance.instructions.md (applyTo: "src/**") ← performance-standards
│
AGENTS.md                         ← agent behavior definitions (loaded as agent context)
```

**Key**: Core rules in `copilot-instructions.md`. Path-specific rules only load when Copilot touches matching file patterns. `AGENTS.md` provides agent behavior.

### Kiro — Spec-Scoped + Steering Separation

```
.kiro/
├── steering/                     ← auto-loaded every session (behavioral rules only)
│   ├── agent-constraints.md      ← always loaded
│   ├── coding-standards.md       ← always loaded
│   └── security-rules.md         ← always loaded
│
├── specs/                        ← per-feature (loaded only when working on that feature)
│   ├── code-review/
│   │   ├── requirements.md       ← includes: code-standards, quality-metrics context
│   │   ├── design.md
│   │   └── tasks.md
│   ├── test-generation/
│   │   ├── requirements.md       ← includes: testing-frameworks, code-standards context
│   │   ├── design.md
│   │   └── tasks.md
│   └── ...
│
├── hooks/                        ← event-driven (not loaded upfront)
│   ├── on-save.json
│   └── on-create.json
```

**Key**: Steering rules are always-loaded but kept to behavioral guidelines only (not detailed context). Specs scope context per-feature — each spec includes only the context its tasks need. Hooks fire on events, not at startup.

---

## Loading Budget Guidelines

| Category | Token Budget | Guideline |
|----------|-------------|-----------|
| Always-loaded core | < 2,000 tokens | `project-overview` + `agent-constraints` only |
| Per-agent context | < 3,000 tokens | Agent's required contexts + rules |
| Path-scoped rules | < 1,000 tokens each | Only activated for matching file patterns |
| Full session max | < 8,000 tokens | Core + one agent's full dependencies |

**Anti-pattern**: Loading all 10 contexts + 8 rules + 9 agents = ~30,000+ tokens before any user code.
**Target**: Core (2K) + task-specific (3-5K) = ~5-7K tokens of instruction context.

---

## Setup Agent Integration

When the setup agent scaffolds a project, it should:

1. Read **this dictionary** first (not the full starter files)
2. Ask the user which **agents** they want to activate
3. Resolve dependencies from the Agent Registry table
4. Load **only** the required definitions from `starter-agents.md`, `starter-rules.md`, `starter-context.md`
5. Generate tool-specific files using native selective loading (see strategies above)
6. Validate token budget per file stays within guidelines
