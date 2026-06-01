---
name: security-policies
type: context
priority: high
tool: claude
loading: on-demand
used-by: [securityScanner, cicdValidator]
---

# Context: Security Policies (Yugastore-Java)

## Summary
Application-security standards for Yugastore. Derived from `.claude/rules/rules.md`
(security-standards) and QE-Core SLAs. These are the enforced expectations for any
change touching `*/src/**` or `react-ui/frontend/src/**`.

## OWASP & Injection
- OWASP Top 10 coverage is a goal (100% tested). Priority: broken access control,
  injection, auth failures, cryptographic failures, vulnerable components.
- **Parameterized/bound queries only** across YCQL (Spring Data Cassandra) and YSQL
  (Spring Data JPA). Never build queries by string concatenation. This is the
  governing rule for the checkout inventory decrement (`UPDATE ... IF quantity >= n`
  via bound statements — no `BEGIN TRANSACTION` string assembly).
- Validate/sanitize all inputs reaching api-gateway (8081) and downstream services.

## Secrets & Logging
- No API keys, passwords, or YugabyteDB connection strings in code or committed
  config — use environment/secrets management (`@Value("${...}")`, env, profiles).
- Never log PII, tokens, or credentials.

## Auth (login-microservice, 8085)
- Passwords hashed with bcrypt/scrypt; rate-limit failed attempts.
- Sessions/tokens expire and are validated; prevent replay and privilege escalation.
- Distinguish authentication vs authorization in review (CWE-862/CWE-863, CWE-639).
- Note: login service is WIP/orphaned today — flag any real auth code carefully.

## Transport & Data
- HTTPS/TLS 1.2+ in transit; encryption at rest AES-256 or equivalent.
- Identify and handle PII explicitly.

## Severity SLAs (QE-Core — block/patch windows)
| Severity | Action |
|----------|--------|
| Critical | 0 allowed — block merge / fix immediately |
| High | patch within 7 days |
| Medium | within 30 days |
| Low | tracked |

## Dependency CVE SLAs (CVSS)
9.0–10.0 fix immediately · 7.0–8.9 within 7 days · 4.0–6.9 within 30 days · < 4.0
track. No known-vulnerable versions in production; replace EOL deps. See
`dependency-mgmt.md` for tooling and license policy.

## Findings format
Cite the vulnerability type (CWE) and explain the attack vector and exploitability,
not just the rule. Attach a confidence score.

## Current State
No SAST (SonarQube) or dependency scanner (Snyk) is wired yet. These policies are
the to-be gates; apply them in review and when CI is introduced.
