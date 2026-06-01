---
applyTo: "resources/**/*.py"
---

# Python Instructions (Yugastore data-loading)

**Tool:** GitHub Copilot · **Scope:** Python 3 sample-data loading scripts in `resources/`

These scripts load sample data (~6K products) into YugabyteDB. They are operational
tooling, not application code — keep them simple and runnable.

- **Style**: PEP 8; clear names; type hints on function signatures where it aids clarity.
- **Targets**: load into YugabyteDB via YCQL (Cassandra API) / YSQL (Postgres API) to match
  `resources/schema.cql` and `resources/schema.sql`. Keep loaders in sync with those schemas.
- **Connections**: never hardcode credentials/hosts — read from env or script args, with
  sensible defaults. Never log credentials.
- **Idempotence/safety**: prefer batched, restartable loads; don't truncate or drop without
  an explicit flag. Don't run loads automatically — they're invoked deliberately
  (`resources/dataload.sh`).
- **Errors**: fail fast with a clear message; don't swallow exceptions silently.
