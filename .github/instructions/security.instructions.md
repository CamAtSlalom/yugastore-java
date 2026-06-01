---
applyTo: "**/src/**,react-ui/frontend/src/**"
---

# Security Standards (Yugastore)

**Tool:** GitHub Copilot · **Source of truth:** `.github/rules/rules.md` (security-standards, Snyk/SonarQube tool rules)

- **OWASP Top 10** coverage is the goal. Priority: broken access control, injection, auth
  failures, cryptographic failures, vulnerable components.
- **Injection**: parameterized queries only across YCQL (Cassandra) and YSQL (JPA). Never
  build queries via string concatenation. Validate/sanitize all inputs reaching
  api-gateway (8081) and downstream services.
- **Secrets**: no API keys, passwords, or DB credentials in code or committed config — use
  environment/secrets management. Never log PII, tokens, or credentials.
- **Auth (login service, 8085)**: hash passwords (bcrypt/scrypt); rate-limit failed
  attempts; expire and validate sessions/tokens; prevent replay and privilege escalation.
  Distinguish authentication from authorization.
- **Transport/data**: HTTPS/TLS 1.2+ in transit; encryption at rest; identify and handle PII.
- **Severity SLAs**: Critical vulnerabilities = 0 (block merge / fix immediately); High
  within 7 days; Medium within 30 days; Low tracked.
- **Dependency CVEs (CVSS)**: 9.0-10.0 fix immediately · 7.0-8.9 within 7 days ·
  4.0-6.9 within 30 days · < 4.0 track. No known-vulnerable or EOL deps in production.
- **Findings**: cite vulnerability type (CWE) and explain the attack vector.

For deep scans, invoke the `security-scanner` agent (`.github/agents/security-scanner.md`).
