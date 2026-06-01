# Extract Decisions

Extract decisions, action items, requirements, technical insights, and open questions from a transcript or document.

## Usage

```
/extract-decisions path/to/transcript-or-document.md
```

**Parameters:**
- Input file path (markdown, plain text, PDF, or DOCX)

## What This Command Does

This command processes a single input and extracts **project-level knowledge**:
- Decisions made (with attribution and confidence)
- Action items (with owners and due dates)
- Feature/requirement refinements
- Technical design insights
- Open questions needing follow-up

Output is written to both a per-input analysis file and merged into consolidated project context files.

## Output Locations

```
docs/processed/{date}_{input-name}.md          # Per-input analysis
docs/context/project/decision_log.md           # Consolidated decisions
docs/context/project/action_items.md           # Consolidated action items
docs/context/project/requirements.md           # Consolidated requirements
docs/context/project/technical_insights.md     # Consolidated technical insights
docs/context/project/open_questions.md         # Consolidated open questions
```

## Processing Strategy

### Step 1: Validate Input

1. Verify input file exists
2. Read the file content
3. Create output directories if they don't exist (`docs/processed/`, `docs/context/project/`)

### Step 2: Read Existing Context

If `docs/context/project/` has existing files:
1. Read current decision log, action items, requirements
2. Note what's already captured to avoid duplicates

### Step 3: Analyze Input

Read the input and extract by category:

#### Decisions

Look for explicit and implicit decisions:
- "We've decided to..." / "Let's go with..."
- "The approach will be..." / "I think we should..." (followed by agreement)
- Lack of objection after a proposal

For each decision capture:
- **Title**: Clear, concise decision name
- **Made By**: Person(s) who made or endorsed it
- **Rationale**: Why this decision was made
- **Implications**: What follows from it
- **Confidence**: High (explicit), Medium (implied agreement), Low (inferred)
- **Source**: Input filename and date

#### Action Items

Look for task assignments:
- "Can you..." / "Will you..." / "I'll take care of..."
- "Action item:" / "We need someone to..."
- "By [date]..." / deadline references

For each action item capture:
- **Owner**: Person assigned (use "TBD" if not specified)
- **Task**: Clear description
- **Due Date**: If mentioned (otherwise "Not specified")
- **Context**: Why this action is needed
- **Source**: Input filename and date

#### Requirements/Refinements

Look for scope and requirement changes:
- "Actually, we also need..." / "Let's deprioritize..."
- "The scope should include/exclude..."
- "That requirement has changed to..."

For each requirement capture:
- **Feature**: Which feature is affected
- **Change Type**: Scope change, Priority change, Requirement update, New requirement
- **Description**: What specifically changed
- **Source**: Input filename and date

#### Technical Insights

Look for technical discussions:
- Architecture discussions
- Technology choices
- Integration patterns
- Performance or security considerations

For each insight capture:
- **Topic**: What technical area
- **Type**: Architecture, Integration, Constraint, Performance, Security
- **Description**: What was discussed or decided
- **Impact**: Systems or components affected
- **Source**: Input filename and date

#### Open Questions

Look for unresolved items:
- "We need to figure out..." / "I'm not sure about..."
- "Let's circle back on..." / "TODO: determine..."
- Questions left unanswered in the discussion

For each question capture:
- **Question**: The unresolved question
- **Context**: Why this came up
- **Suggested Owner**: Who should answer (if mentioned)
- **Source**: Input filename and date

### Step 4: Determine Output Filename

Generate: `{YYYY-MM-DD}_{input-name}.md`

1. Extract date from filename (if date pattern found) or from content, or use today's date
2. Clean input filename: lowercase, replace spaces with hyphens, truncate to ~50 chars

### Step 5: Write Per-Input Analysis

Write to `docs/processed/{output_filename}` following the template in `ae-toolkit/modules/context-extraction/templates/PROCESSED_OUTPUT.template.md`.

### Step 6: Merge Into Project Context Files

For each context file:
1. Read existing content (if file exists)
2. Append new items (don't duplicate existing entries)
3. Preserve source attribution on all items

**decision_log.md**: Append each decision as a new section
**action_items.md**: Append each action item as a new table row
**requirements.md**: Append each requirement under appropriate feature heading
**technical_insights.md**: Append each insight as a new section
**open_questions.md**: Append each question as a new numbered item

### Step 7: Update Processing Log

Append to `docs/processing_log.md`:
```markdown
| {date} | {filename} | {files_updated} | {brief_summary} |
```

### Step 8: Report Summary

```
Extraction Complete

Source: {filename}

Extracted:
- Decisions: {count}
- Action Items: {count}
- Requirements: {count}
- Technical Insights: {count}
- Open Questions: {count}

Output: docs/processed/{output_filename}

Files Updated:
- docs/context/project/decision_log.md
- docs/context/project/action_items.md
- ...
```

## Long Input Handling

For inputs over 800 lines:
1. Warn user about length
2. Process in logical chunks (topic changes, agenda items)
3. Maintain running summary across chunks
4. Consolidate findings, resolve contradictions (later statements supersede earlier)

## Success Criteria

- [ ] Input read successfully
- [ ] All extraction categories processed
- [ ] Per-input analysis file created
- [ ] Project context files updated (merge, not overwrite)
- [ ] Source attribution on all items
- [ ] Processing log updated
- [ ] Summary reported to user
