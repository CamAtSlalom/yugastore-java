# Discover Analyze

Perform detailed functional analysis of codebase components, producing per-component markdown documentation.

## Usage

```
/discover-analyze target: /path/to/repo [component: component-key]
```

**Parameters:**
- `target`: Absolute path to the codebase being analyzed
- `component` (optional): Specific component key to analyze. If omitted, analyzes all components.

## What This Command Does

This is **Phase 2** of the Discover workflow. It reads the component inventory and performs comprehensive functional analysis of each component (or a specific one), producing detailed documentation about:

- **Business Logic**: Core business rules, decision points, validation logic
- **Data Handling**: Input/output structures, transformations, database interactions
- **Dependencies**: Internal and external dependencies, configuration
- **Integration Points**: What calls this component and what it calls
- **Error Handling**: Exception types, recovery mechanisms, logging patterns

## Prerequisites

`docs/context/codebase/{repo-name}/components.json` must exist (produced by Phase 1).

## Output

One markdown file per component:
```
docs/context/codebase/{repo-name}/component_analysis/{key}.md
```

## Iterative Write Approach

**DO NOT wait until the end to write the analysis.**

Follow an iterative, write-as-you-go approach:

1. **Create the output file immediately** with template section headers
2. **Write each section as you complete analysis**:

| Analysis Phase | Write These Sections | When |
|----------------|---------------------|------|
| Context | Overview, metadata | After reading component files |
| Structure | Class hierarchy, public API | After analyzing structure |
| Logic | Business rules, decision points, validation | As each rule is discovered |
| Data | Data handling, transformations, database | As data flows are traced |
| Integration | Dependencies, integration points | After mapping connections |
| Notes | Key insights, SME questions | Throughout and at end |

3. **No placeholder text** — write real content for each section

## Analysis Process

### For Each Component:

**1. Context Gathering**
- Read the primary file at `file_path`
- Read all files in `related_files`
- Understand the component's role in the broader system

**2. Structure Analysis**
- Document class hierarchy (parent classes, interfaces, inner classes)
- Catalog public API (key methods, signatures, annotations)

**3. Business Logic Analysis**
- Identify core business rules (conditions → behaviors)
- Map decision points (branching, state transitions)
- Document validation logic (input, business rule, data integrity)

**4. Data Handling Analysis**
- Trace inputs (parameters, database reads, API calls)
- Trace outputs (return values, database writes, API calls)
- Document transformations (mappings, aggregations, calculations)

**5. Integration Analysis**
- Map internal dependencies (what this depends on, what depends on this)
- Map external dependencies (libraries, services, databases)
- Document APIs exposed and consumed

**6. Final Documentation**
- Capture key insights and unexpected behaviors
- Document SME questions (unclear logic, missing context, assumptions)
- Note recommendations (deeper investigation, migration considerations)

## Template Structure

Each component analysis file should follow the structure in `ae-toolkit/modules/codebase-discover/templates/COMPONENT_ANALYSIS.template.md`. Key sections:

```markdown
# {Component Name}

## Overview
## Class Structure
## Public API
## Business Logic Analysis
## Data Handling
## Dependencies
## Integration Points
## Error Handling
## Key Insights
## SME Questions
```

## Quality Guidelines

- **Business perspective**: Focus on WHAT the code does, not just how
- **Code references**: Include file paths and method names for traceability
- **No speculation**: If logic is unclear, document as an SME question
- **Scope focus**: Stay within the component's files — only explore broader codebase when necessary for understanding

## Success Criteria

- [ ] All component files read and analyzed
- [ ] Output file created at correct path
- [ ] Overview captures component purpose clearly
- [ ] Business rules identified with code references
- [ ] Data flows documented
- [ ] Dependencies mapped (internal and external)
- [ ] SME questions documented for unclear logic
