# Classify Input

Analyze a transcript or document and determine what type of knowledge it contains, without performing extraction.

## Usage

```
/classify-input path/to/transcript-or-document.md
```

**Parameters:**
- Input file path (markdown, plain text, PDF, or DOCX)

## What This Command Does

This is a **preview command** — it classifies the input and reports what it contains without writing any output files. Use it to understand what processing will produce before committing.

## Classification Process

### Step 1: Read the Input

1. Read the file at the provided path
2. If file not found, report error

### Step 2: Scan for Content Signals

Scan for signals in each category:

**Enterprise Context Signals:**

| Category | Signals |
|----------|---------|
| Process | "step process", "phase", "lifecycle", "workflow", "gate review", "out of band" |
| Approvals | "approval", "scope lock", "sign off", "gate", "formal review" |
| Roles | "product manager", "architect", "tech lead", "project manager" (in generic context) |
| Teams/Org | "squad", "team structure", "organization", "value stream", "team ownership" |
| Tooling | "JIRA", "Confluence", "ServiceNow", tool names, workflow systems |
| Conventions | "sizing", "T-shirt", "naming convention", "sprint cadence", "hierarchy" |
| Applications | "how {app} works", "tech stack", "built with", system walkthroughs |

**Project Context Signals:**

| Category | Signals |
|----------|---------|
| Decisions | "we decided", "let's go with", "the approach will be", "agreed to" |
| Requirements | "requirement", "acceptance criteria", "user story", "scope for this" |
| Technical Design | "architecture for", "this service will", "API design", "data model" |
| Action Items | "action item", "will follow up", deadlines, assignments |
| Features | Specific feature names, "MVP scope", "phase 1 deliverables" |

### Step 3: Score and Classify

| Enterprise Score | Project Score | Classification |
|------------------|---------------|----------------|
| High (5+) | Low (0-2) | `enterprise` |
| Low (0-2) | High (5+) | `project` |
| Any (3+) | Any (3+) | `dual` |
| Low (0-2) | Low (0-2) | `unclear` |

### Step 4: Report Classification

Return a structured report:

```
Classification: {enterprise|project|dual|unclear}

Enterprise Signals (score: {N}):
- Process: {signals found}
- Roles: {signals found}
- Tooling: {signals found}

Project Signals (score: {N}):
- Decisions: {signals found}
- Action Items: {signals found}
- Requirements: {signals found}

Recommended Processing:
- /extract-context (enterprise knowledge)
- /extract-decisions (project knowledge)

Notes: {any observations about the content}
```

## When to Use This Command

- **Before batch processing** — preview what a set of documents will produce
- **Uncertain content** — when you're not sure if a document is project or enterprise
- **Selective processing** — when you want to run only specific extractors

## Output

This command produces **no files** — it only reports the classification to the user. To actually extract content, use `/process-input` or the individual extraction commands.
