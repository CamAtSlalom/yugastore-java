# Discover Synthesize

Synthesize all component analyses into summary documentation — executive summary, navigation index, and technology stack.

## Usage

```
/discover-synthesize target: /path/to/repo
```

**Parameters:**
- `target`: Absolute path to the codebase that was analyzed

## What This Command Does

This is **Phase 3** of the Discover workflow. It reads all component analyses and produces synthesis documentation that creates a coherent picture of the entire system.

## Prerequisites

The following must exist at `docs/context/codebase/{repo-name}/`:
- `components.json` (from Phase 1)
- `component_analysis/*.md` files (from Phase 2)

## Output Files

```
docs/context/codebase/{repo-name}/
├── ANALYSIS_SUMMARY.md           # Executive summary
├── ANALYSIS_INDEX.md             # Navigation index
└── architecture/
    └── tech_stack.md             # Technology stack details
```

## Synthesis Process

### Step 1: Read All Analyses

1. Read `components.json` to understand component inventory
2. Read each file in `component_analysis/`
3. Build mental model of system architecture, business capabilities, integration patterns, and technology stack

### Step 2: Create ANALYSIS_SUMMARY.md

Write a comprehensive executive summary:

```markdown
# Analysis Summary: {System Name}

## Executive Summary
[2-3 paragraphs: system purpose, architecture, key capabilities]

## System Architecture
### High-Level Architecture
### Component Overview
### Data Flow

## Key Business Capabilities
### {Capability 1}
- Purpose, components, integration points

## Integration Points
### External Systems
| System | Purpose | Integration Type | Components |
### Internal Patterns

## Technology Stack Summary

## Risk Assessment
### High Complexity Areas
### Technical Debt Indicators
### Migration Considerations

## Key Findings
### Patterns Discovered
### Anti-Patterns Found
### Recommendations

## Questions for SME
[Aggregated from all component analyses]

## Next Steps
```

### Step 3: Create ANALYSIS_INDEX.md

Create a navigation index:

```markdown
# Analysis Index

## Summary Documents
- [Analysis Summary](./ANALYSIS_SUMMARY.md)
- [Technology Stack](./architecture/tech_stack.md)

## Component Analyses

### By Functional Area
#### {Area 1}
- [{Component}](./component_analysis/{key}.md) - Brief description

### By Priority
#### Critical
#### High
#### Medium
#### Low

### By Type
#### Services
#### Controllers
#### Repositories

## Statistics
- Total Components Analyzed: X
- Critical Components: X
- Functional Areas Covered: X
```

### Step 4: Create architecture/tech_stack.md

Document the technology stack:

```markdown
# Technology Stack

## Language & Runtime
## Frameworks
## Data Layer
### Databases
### ORM/Data Access
## Integration Technologies
### API Technologies
### Message Queues
### External Clients
## Build & Deployment
## Security
### Authentication
### Authorization
## Observability
### Logging
### Monitoring
## Dependencies Summary
### Critical Dependencies
### Deprecated/Outdated
```

## Quality Guidelines

### Synthesize, Don't Just Aggregate
- Create NEW insights from combined analyses
- Identify patterns spanning multiple components
- Document integration patterns used across the system
- Note shared dependencies and configurations

### Make It Actionable
- Provide clear next steps
- Highlight risks with specific recommendations
- Make navigation easy with consistent linking

## Success Criteria

- [ ] All component analyses read and incorporated
- [ ] `ANALYSIS_SUMMARY.md` has substantive executive summary
- [ ] `ANALYSIS_INDEX.md` links correctly to all analyses
- [ ] `architecture/tech_stack.md` covers full technology stack
- [ ] SME questions aggregated from all analyses
- [ ] Statistics are accurate
- [ ] Cross-cutting patterns identified and documented
