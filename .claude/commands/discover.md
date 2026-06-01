# Discover

Orchestrate a full three-phase codebase analysis workflow: inventory, analyze, and synthesize.

## Usage

```
/discover target: /path/to/repo [spec: /path/to/spec.yaml]
```

**Parameters:**
- `target`: Absolute path to the codebase to analyze
- `spec` (optional): Path to a Discovery Specification YAML file

## What This Command Does

This is the **orchestrator** that runs all three Discover phases in sequence:

1. **Inventory** (`/discover-inventory`) — Scan the codebase and produce `components.json`
2. **Analyze** (`/discover-analyze`) — Deep-dive each component, writing per-component markdown
3. **Synthesize** (`/discover-synthesize`) — Produce executive summary, index, and tech stack docs

All output is written to `docs/context/codebase/{repo-name}/` where `{repo-name}` is the target directory name.

## Execution Flow

### Phase 1: Inventory

1. Read build files and project structure
2. Identify architectural patterns and entry points
3. Group components into 5-15 functional units
4. Write `docs/context/codebase/{repo-name}/components.json`

**CHECKPOINT**: Present inventory to user for review. Ask:
- "Does this grouping make sense?"
- "Are there areas I missed or should split differently?"

Proceed to Phase 2 only after user acknowledges.

### Phase 2: Analyze

For each component in priority order (critical → high → medium → low):
1. Read primary file and related files
2. Create `component_analysis/{key}.md` immediately
3. Write sections iteratively: overview, structure, business logic, data handling, dependencies, integration, notes
4. Document SME questions for unclear logic

**CHECKPOINT**: Summarize findings. Flag any components needing SME input.

### Phase 3: Synthesize

1. Read all component analyses
2. Create `ANALYSIS_SUMMARY.md` — executive summary, architecture, capabilities, risks
3. Create `ANALYSIS_INDEX.md` — navigation by area, priority, type
4. Create `architecture/tech_stack.md` — detailed technology documentation

**CHECKPOINT**: Present summary highlights and aggregated SME questions.

## Discovery Specification

If a `spec.yaml` is provided, use it in all phases:
- **Inventory**: Apply `exclude_patterns`, `component_classification`, `flows` for grouping
- **Analyze**: Focus on `analysis_focus.emphasize` areas, answer `key_questions`, use `flows` for context
- **Synthesize**: Include flow-based organization, reference `architecture` context

If no spec is provided, use auto-detection and default heuristics.

## Output Structure

```
docs/context/codebase/{repo-name}/
├── components.json
├── ANALYSIS_SUMMARY.md
├── ANALYSIS_INDEX.md
├── architecture/
│   └── tech_stack.md
└── component_analysis/
    ├── {component-key-1}.md
    ├── {component-key-2}.md
    └── ...
```

## Success Criteria

- [ ] `components.json` has 5-15 grouped components
- [ ] Every component has a substantive analysis file
- [ ] Executive summary captures system purpose, architecture, risks
- [ ] Navigation index links correctly to all analyses
- [ ] Tech stack document is comprehensive
- [ ] SME questions aggregated in summary
