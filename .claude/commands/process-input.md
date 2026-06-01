# Process Input

Orchestrate the processing of a transcript or document by classifying content and routing to appropriate extractors.

## Usage

```
/process-input path/to/transcript-or-document.md
```

**Parameters:**
- Input file path (markdown, plain text, PDF, or DOCX)

## What This Command Does

This is the **orchestrator** that processes any input through the full extraction pipeline:

1. **Classify** — Determine what type of knowledge the input contains
2. **Extract Project Context** — Decisions, action items, requirements, technical insights, open questions
3. **Extract Enterprise Context** — Process definitions, roles, team structures, tooling, conventions
4. **Consolidate** — Update processing log, generate summary

## Execution Flow

### Step 1: Read and Classify Input

1. Read the input file
2. Scan for content signals (project vs enterprise)
3. Score and classify: `project`, `enterprise`, `dual`, or `unclear`

**If `unclear`**: Present findings to user with options:
- [P] Process as project context only
- [E] Process as enterprise context only
- [B] Process both
- [S] Skip processing

### Step 2: Read Existing Context

Before extracting, read existing context to inform merge behavior:
- If classified as `project` or `dual`: Read `docs/context/project/` files
- If classified as `enterprise` or `dual`: Read `docs/context/enterprise/` files

### Step 3: Extract Project Context (if applicable)

For inputs classified as `project` or `dual`:

1. Extract **decisions** — explicit and implicit, with confidence levels
2. Extract **action items** — tasks, owners, due dates
3. Extract **requirements** — feature changes, scope refinements
4. Extract **technical insights** — architecture, integration, constraints
5. Extract **open questions** — unresolved items needing follow-up

Write per-input analysis to `docs/processed/{date}_{input-name}.md`

Merge into:
- `docs/context/project/decision_log.md`
- `docs/context/project/action_items.md`
- `docs/context/project/requirements.md`
- `docs/context/project/technical_insights.md`
- `docs/context/project/open_questions.md`

### Step 4: Extract Enterprise Context (if applicable)

For inputs classified as `enterprise` or `dual`:

1. Extract **process information** — SDLC steps, approval workflows
2. Extract **organization info** — roles, teams, applications
3. Extract **tooling** — systems, integrations, usage patterns
4. Extract **conventions** — naming, sizing, cadence patterns

Merge into:
- `docs/context/enterprise/process/sdlc_process.md`
- `docs/context/enterprise/process/approval_workflows.md`
- `docs/context/enterprise/organization/roles_and_responsibilities.md`
- `docs/context/enterprise/organization/teams_and_applications.md`
- `docs/context/enterprise/tooling/tooling.md`

Flag contradictions → `docs/context/enterprise/things_to_clarify.md`

### Step 5: Update Processing Log

Append to `docs/processing_log.md`:

```markdown
| {date} | {input_filename} | {files_updated} | {brief_summary} |
```

### Step 6: Generate Summary

Report to user:

```
Input Processing Complete

Source: {filename}
Classification: {project|enterprise|dual}

Extracted:
- Decisions: {count}
- Action Items: {count}
- Requirements: {count}
- Technical Insights: {count}
- Open Questions: {count}
- Enterprise Process: {sections added/updated}
- Enterprise Org: {sections added/updated}
- Enterprise Tooling: {sections added/updated}

Files Modified: {list}
Conflicts Found: {count} (logged to things_to_clarify.md)

Next Steps:
- Review extracted content for accuracy
- Resolve any conflicts in things_to_clarify.md
- Process additional inputs to build knowledge base
```

## Merge Strategy

When updating existing files:
1. **Add** new sections that don't exist
2. **Enhance** existing sections with new details
3. **Never replace** existing content
4. **Flag conflicts** with `[CONFLICT]` marker when new info contradicts existing
5. **Preserve attribution** — include source file and date on all items

## Directory Creation

If output directories don't exist, create them:
- `docs/context/project/`
- `docs/context/enterprise/process/`
- `docs/context/enterprise/organization/`
- `docs/context/enterprise/tooling/`
- `docs/processed/`

## Error Handling

| Situation | Action |
|-----------|--------|
| File not found | Report error, suggest checking path |
| Empty file | Report, create minimal output noting the issue |
| Classification unclear | Ask user to choose processing path |
| Very long input (>800 lines) | Warn user, process in chunks |

## Success Criteria

- [ ] Input read and classified
- [ ] Per-input analysis written to `docs/processed/`
- [ ] Project context updated (if applicable)
- [ ] Enterprise context updated (if applicable)
- [ ] Conflicts flagged and logged
- [ ] Processing log updated
- [ ] Summary generated
