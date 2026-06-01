# Extract Context

Extract enterprise-level organizational knowledge from a transcript or document — processes, roles, team structures, tooling, and conventions.

## Usage

```
/extract-context path/to/transcript-or-document.md
```

**Parameters:**
- Input file path (markdown, plain text, PDF, or DOCX)

## What This Command Does

This command extracts **enterprise-level knowledge** that is reusable across engagements:
- SDLC processes and phase definitions
- Approval workflows and governance
- Role definitions and responsibilities
- Team structures and application ownership
- Tooling and systems
- Naming conventions, sizing, cadence patterns

Unlike `/extract-decisions` which captures engagement-specific items, this command captures organizational knowledge that applies broadly.

## Output Locations

```
docs/context/enterprise/
├── process/
│   ├── sdlc_process.md                # Development lifecycle
│   └── approval_workflows.md          # Who approves what, when
├── organization/
│   ├── roles_and_responsibilities.md  # Role definitions
│   └── teams_and_applications.md      # Team ownership, app inventory
├── tooling/
│   └── tooling.md                     # Systems, integrations, conventions
└── things_to_clarify.md               # Discrepancies for SME resolution
```

## Processing Strategy

### Step 1: Validate Input

1. Verify input file exists
2. Read the file content
3. Confirm this contains enterprise-level content (not purely project-specific)
   - If purely project-specific, suggest using `/extract-decisions` instead
   - Allow user to continue if they confirm

### Step 2: Check Enterprise Context Directory

1. Create `docs/context/enterprise/` structure if it doesn't exist
2. Read existing context files to understand what's already captured

### Step 3: Categorize Content

Scan input to identify which categories have relevant content:

| Category | Signals |
|----------|---------|
| SDLC Process | "step process", "phase", "discover", "define", "plan", "deliver", "lifecycle" |
| Approval Workflows | "approval", "scope lock", "sign off", "gate review", "formal review" |
| Roles | Role titles in generic context, "responsible for", "owns" |
| Teams | "squad", "team", "organization", "value stream", "functional area" |
| Tooling | Tool names, "we use", "integrated with", "workflow in" |
| Conventions | "sizing", "naming convention", "sprint cadence", "T-shirt", "hierarchy" |

### Step 4: Extract Process Information

For SDLC process content, extract per step:

```markdown
### Step {N}: {Name}

**Purpose**: {What this step accomplishes}
**Owner**: {Primary role responsible}

**Inputs**:
- {What comes into this step}

**Activities**:
- {What happens during this step}

**Outputs**:
- {Artifacts or decisions produced}

**Exit Criteria**: {What must be true to move to next step}
**Typical Duration**: {If mentioned}
```

For approval workflows:

```markdown
### {Approval Type}

**Trigger**: {When this approval is needed}
**Approver(s)**: {Who approves}
**Mechanism**: {How approval is recorded}
**Artifacts Required**: {What documents are needed}
**Outcome**: {What happens after approval}
```

### Step 5: Extract Organization Information

For roles:

```markdown
### {Role Name} ({Abbreviation})

**Full Title**: {Complete title}
**Responsibilities**:
- {What this role does}
**Scope**: {Initiative level, application level, etc.}
**Reports To**: {If mentioned}
**Works With**: {Related roles}
```

For teams and applications:

```markdown
### {Team/Application Name}

**Type**: {Team, Application, Service}
**Owner**: {Role or person}
**Responsibilities**:
- {What this team/app handles}
**Related Teams**: {Dependencies, collaborators}
```

### Step 6: Extract Tooling Information

```markdown
### {Tool Name}

**Purpose**: {What it's used for}
**Used By**: {Which roles/phases}
**Key Features**:
- {Relevant capabilities}
**Integration Points**: {How it connects to other tools}
**Notes**: {Important context}
```

### Step 7: Extract Conventions

```markdown
### {Convention Name}

**Definition**: {What it means}
**Values**: {Possible values if enumerated}
**Usage**: {When/how it's used}
**Implications**: {What it affects — timeline, budget, etc.}
```

### Step 8: Write or Update Context Files

For each category with extracted content:

1. Read existing file (if it exists)
2. **Merge** new content:
   - Add new sections that don't exist
   - Enhance existing sections with new details (append, don't replace)
   - Note conflicts if new info contradicts existing
3. Preserve source attribution (input filename, date)

### Step 9: Log Discrepancies

If the input contradicts existing context files:

1. Add entry to `docs/context/enterprise/things_to_clarify.md`
2. Include both sources and what each says
3. Add `[CONFLICT]` marker in the affected context file

**When to log**:
- Input says something different than existing context files
- Role definitions or ownership differ between sources
- Process steps described differently
- Terminology conflicts

**When NOT to log**:
- New information that doesn't conflict (just add it)
- More detailed information expanding on existing content
- Minor wording differences with same meaning

### Step 10: Update Processing Log

Append to `docs/processing_log.md`:
```markdown
| {date} | {filename} | {files_updated} | {brief_summary} |
```

### Step 11: Generate Summary

```
Enterprise Context Extracted

Source: {filename}

Content Added/Updated:

Process:
  - sdlc_process.md: {what was added}
  - approval_workflows.md: {what was added}

Organization:
  - roles_and_responsibilities.md: {what was added}
  - teams_and_applications.md: {what was added}

Tooling:
  - tooling.md: {what was added}

Files Modified: {count}
New Sections Added: {count}
Sections Enhanced: {count}

Discrepancies Found: {count}
  - {topic}: {brief description} → logged to things_to_clarify.md

Next Steps:
  - Review generated content for accuracy
  - Resolve discrepancies with SMEs
  - Process additional inputs to fill gaps
```

## Merge Strategy

**Critical**: This command MERGES into existing files. It never overwrites.

1. Read current file content
2. Compare new extractions against existing sections
3. Add new sections that don't exist
4. Enhance existing sections with new details (append under same heading)
5. Flag conflicts with `[CONFLICT]` marker
6. Always include source attribution: `(Source: {filename}, {date})`

## Success Criteria

- [ ] Input read successfully
- [ ] Enterprise context directory exists/created
- [ ] Content categorized correctly
- [ ] Process steps extracted (if present)
- [ ] Roles extracted (if present)
- [ ] Teams/applications extracted (if present)
- [ ] Tooling extracted (if present)
- [ ] Conventions extracted (if present)
- [ ] Files created/updated with merge strategy
- [ ] Source attribution preserved
- [ ] Discrepancies logged (if any found)
- [ ] Processing log updated
- [ ] Summary generated
