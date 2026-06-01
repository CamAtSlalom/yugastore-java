---
description: Initialize a new specification with detailed project description
allowed-tools: Bash, Read, Write, Glob, mcp__atlassian__getJiraIssue, mcp__atlassian__getAccessibleAtlassianResources
argument-hint: <project-description-or-jira-url>
---

# Spec Initialization

<background_information>

- **Mission**: Initialize the first phase of spec-driven development by creating directory structure and metadata for a new specification
- **Success Criteria**:
  - Generate appropriate feature name from project description or Jira ticket
  - Create unique spec structure without conflicts
  - Extract requirements from Jira ticket if provided (description, AC, linked tickets)
  - Provide clear path to next phase (requirements generation)
    </background_information>

<instructions>
## Core Task
Generate a unique feature name from the project description ($ARGUMENTS) or Jira ticket and initialize the specification structure.

## Execution Steps

1. **Detect Input Type**:
   - Check if $ARGUMENTS contains a Jira ticket identifier:
     - Jira URL pattern: `https://[domain].atlassian.net/browse/[KEY-123]`
     - Jira key pattern: `[A-Z]+-[0-9]+` (e.g., NAME-7463, PROJ-123)
   - If Jira ticket detected, proceed to Step 1a (Fetch Jira)
   - Otherwise, proceed to Step 2 (Check Uniqueness) using $ARGUMENTS as description

1a. **Fetch Jira Ticket** (if Jira detected):

- Extract Jira key from URL or use key directly
- Use `mcp__atlassian__getAccessibleAtlassianResources` to get cloudId
- Use `mcp__atlassian__getJiraIssue` with cloudId and issueIdOrKey
- Extract relevant fields:
  - **summary** → Use for feature name generation
  - **description** → Primary project description
  - **Acceptance Criteria** → Extract from description if present (look for "Acceptance Criteria", "AC:", "Success Criteria" sections)
  - **issuelinks** → Note any linked/blocked-by tickets for context
- Construct enriched description:

  ```
  # {Jira Summary}

  Source: {Jira URL}

  ## Description
  {Jira Description}

  ## Acceptance Criteria
  {Extracted AC or "See Jira ticket for details"}

  ## Related Tickets
  {List of linked tickets if any}
  ```

2. **Check Uniqueness**: Verify `.sdd/specs/` for naming conflicts (append number suffix if needed)

3. **Create Directory**: `.sdd/specs/[feature-name]/`

4. **Initialize Files Using Templates**:
   - Read `.sdd-settings/templates/specs/init.json`
   - Read `.sdd-settings/templates/specs/requirements-init.md`
   - Replace placeholders:
     - `{{FEATURE_NAME}}` → generated feature name
     - `{{TIMESTAMP}}` → current ISO 8601 timestamp
     - `{{PROJECT_DESCRIPTION}}` → enriched description (from Jira or original $ARGUMENTS)
     - `{{JIRA_TICKET}}` → Jira key if detected (e.g., "NAME-7463"), or null if not from Jira
   - Write `spec.json` and `requirements.md` to spec directory

## Important Constraints

- DO NOT generate requirements/design/tasks at this stage
- Follow stage-by-stage development principles
- Maintain strict phase separation
- Only initialization is performed in this phase
- Jira ticket detection is automatic - no flags required
- If Jira fetch fails, fall back to using $ARGUMENTS as plain description
  </instructions>

## Tool Guidance

- Use **Glob** to check existing spec directories for name uniqueness
- Use **Read** to fetch templates: `init.json` and `requirements-init.md`
- Use **mcp**atlassian**getAccessibleAtlassianResources** to get cloudId for Jira API calls
- Use **mcp**atlassian**getJiraIssue** to fetch ticket details when Jira key/URL detected
- Use **Write** to create spec.json and requirements.md after placeholder replacement
- Perform validation before any file write operation

## Output Description

Provide output in the language specified in `spec.json` with the following structure:

1. **Source**: Indicate if initialized from Jira ticket (with link) or plain description
2. **Generated Feature Name**: `feature-name` format with 1-2 sentence rationale
3. **Project Summary**: Brief summary (1 sentence)
4. **Created Files**: Bullet list with full paths
5. **Next Step**: Command block showing `/sdd:spec-requirements <feature-name>`
6. **Notes**: Explain why only initialization was performed (2-3 sentences on phase separation)

**Format Requirements**:

- Use Markdown headings (##, ###)
- Wrap commands in code blocks
- Keep total output concise (under 300 words)
- Use clear, professional language per `spec.json.language`

## Safety & Fallback

- **Jira Fetch Failure**: If Jira API call fails, log warning and fall back to using $ARGUMENTS as plain description. Notify user that Jira integration failed but initialization will continue.
- **Ambiguous Feature Name**: If feature name generation is unclear, propose 2-3 options and ask user to select
- **Template Missing**: If template files don't exist in `.sdd-settings/templates/specs/`, report error with specific missing file path and suggest checking repository setup
- **Directory Conflict**: If feature name already exists, append numeric suffix (e.g., `feature-name-2`) and notify user of automatic conflict resolution
- **Write Failure**: Report error with specific path and suggest checking permissions or disk space
- **Jira Key Detection**: Use regex pattern `[A-Z]{2,}-\d+` for Jira keys. Common patterns: PROJ-123, NAME-7463, etc.
