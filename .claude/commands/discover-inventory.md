# Discover Inventory

Scan a target codebase and produce a structured JSON inventory of components for subsequent analysis phases.

## Usage

```
/discover-inventory target: /path/to/repo [spec: /path/to/spec.yaml]
```

**Parameters:**
- `target`: Absolute path to the codebase to analyze
- `spec` (optional): Path to a Discovery Specification YAML file

## What This Command Does

This is **Phase 1** of the Discover workflow. It analyzes a target codebase and produces a structured JSON inventory of components grouped by functional area.

**Your output is a single JSON file** at `docs/context/codebase/{repo-name}/components.json`.

## Output Format

```json
{
  "components": [
    {
      "key": "pricing-pipeline",
      "name": "Pricing Pipeline Components",
      "type": "service",
      "file_path": "src/main/java/com/example/pricing/PricingPipeline.java",
      "priority": "critical",
      "description": "Core service orchestrating the pricing calculation pipeline",
      "related_files": [
        "src/main/java/com/example/pricing/handlers/TransformHandler.java",
        "src/main/java/com/example/pricing/handlers/EligibilityHandler.java"
      ],
      "functional_area": "pricing"
    }
  ],
  "total_count": 12,
  "grouped_by": "functional_area",
  "analysis_scope": "Core business logic components excluding utilities and generated code"
}
```

**Required fields**: key, name, type, file_path, priority
**Optional fields**: description, related_files, functional_area

**Valid types**: `api`, `service`, `controller`, `repository`, `model`, `utility`, `integration`, `ui`, `other`
**Valid priorities**: `critical`, `high`, `medium`, `low`

## Component Granularity

**Target 5-15 analysis units** — NOT 50+ individual classes.

**Group components by:**
- Functional area (pricing, orders, customers, integrations)
- Package/namespace (related classes in same package)
- Feature (classes implementing a single feature together)
- Layer (controllers, services, repositories — if layer-based grouping fits)

**GOOD grouping:**
```json
{
  "key": "pricing-pipeline",
  "name": "Pricing Pipeline Components",
  "type": "service",
  "file_path": "src/main/java/com/example/pricing/PricingPipeline.java",
  "related_files": [
    "src/main/java/com/example/pricing/handlers/TransformHandler.java",
    "src/main/java/com/example/pricing/handlers/EligibilityHandler.java",
    "src/main/java/com/example/pricing/handlers/DiscountHandler.java"
  ],
  "functional_area": "pricing"
}
```

**BAD granularity (too fine-grained):**
```json
{"key": "transform-handler", ...},
{"key": "eligibility-handler", ...},
{"key": "discount-handler", ...}
```

## Analysis Strategy

### Step 1: Codebase Structure Discovery

1. Read build files (`pom.xml`, `build.gradle`, `package.json`, `pyproject.toml`, `go.mod`, `Cargo.toml`)
2. Identify main source directories and multi-module structure
3. Detect architectural patterns (layered, chain of responsibility, event-driven)
4. Find configuration and entry points

### Step 2: Component Identification

1. Scan for key component types based on detected framework:
   - **Java/Spring**: `@Service`, `@RestController`, `@Repository`, Feign clients
   - **Python/FastAPI**: routers, services, repositories, Pydantic models
   - **TypeScript/React**: pages, providers, hooks, API routes
   - **Go**: handlers, servers, clients, middleware
2. Group related components by functional area
3. Assign priorities based on business criticality

### Step 3: Apply Spec (if provided)

If a Discovery Specification is provided:
- Apply `exclude_patterns` to skip irrelevant directories
- Use `component_classification.priority_rules` to override default priorities
- Use `flows` to understand which components belong to which execution paths
- Apply `active_code_detection` strategy to deprioritize dead code

### Step 4: Write Inventory

1. Create `docs/context/codebase/{repo-name}/components.json`
2. Validate JSON structure and required fields
3. Confirm 5-15 component groups

## What to Exclude

- Generated code (unless it IS the primary implementation)
- Test code (focus on production code)
- Build artifacts and dependencies
- Configuration-only files (unless they define significant behavior)
- Patterns matching `exclude_patterns` from spec

## What to Include

- Core business logic classes
- API endpoints and controllers
- Data access layer components
- External integration clients
- Significant utility classes used across the codebase

## Success Criteria

- [ ] Codebase structure understood (build files, source directories)
- [ ] Architectural patterns identified
- [ ] Components grouped by functional area (5-15 groups)
- [ ] Priorities assigned based on business criticality
- [ ] `docs/context/codebase/{repo-name}/components.json` created with valid JSON
- [ ] All required fields populated with valid enum values
- [ ] File paths are relative to codebase root
