#!/usr/bin/env python3
"""Yugastore PreToolUse guard.

Runs before Write/Edit/MultiEdit. Enforces two repo invariants from
.claude/rules/ :
  1. Spring Boot 2.6 => javax.* (NOT jakarta.*). jakarta.* in a .java file
     breaks the build -> HARD BLOCK (exit 2).
  2. No hardcoded secrets/credentials in source or config -> WARN (exit 0,
     surfaced to the transcript) to avoid false-positive blocks on local
     defaults / test fixtures.

Protocol: read tool call JSON on stdin. Exit 2 + stderr blocks the tool call
and feeds the message back to Claude; exit 0 allows it.
"""
import json
import re
import sys


def _collect_new_text(tool_input):
    """Return the text this tool call is about to introduce."""
    texts = []
    if isinstance(tool_input.get("content"), str):          # Write
        texts.append(tool_input["content"])
    if isinstance(tool_input.get("new_string"), str):       # Edit
        texts.append(tool_input["new_string"])
    for edit in tool_input.get("edits", []) or []:          # MultiEdit
        if isinstance(edit, dict) and isinstance(edit.get("new_string"), str):
            texts.append(edit["new_string"])
    return "\n".join(texts)


SECRET_PATTERNS = [
    (r"AKIA[0-9A-Z]{16}", "AWS access key id"),
    (r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----", "private key material"),
    (r"(?i)(?:secret|api[_-]?key|access[_-]?token)\s*[:=]\s*[\"'][A-Za-z0-9_\-]{16,}[\"']",
     "hardcoded secret / api key / token"),
    (r"(?i)password\s*[:=]\s*[\"'][^\"']{6,}[\"']", "hardcoded password literal"),
]


def main():
    try:
        data = json.load(sys.stdin)
    except Exception:
        sys.exit(0)  # never block on malformed hook input

    tool_input = data.get("tool_input") or {}
    path = tool_input.get("file_path", "") or ""
    blob = _collect_new_text(tool_input)
    if not blob:
        sys.exit(0)

    # 1) Hard block: jakarta.* in Java sources.
    if path.endswith(".java") and re.search(r"^\s*import\s+jakarta\.", blob, re.MULTILINE):
        sys.stderr.write(
            "BLOCKED by .claude/hooks/guard.py:\n"
            f"- jakarta.* import in {path}. This repo is Spring Boot 2.6.3 -> use "
            "javax.* (javax.persistence / javax.servlet). jakarta.* breaks the build. "
            "See .claude/rules/java-spring.md.\n"
        )
        sys.exit(2)

    # 2) Warn (non-blocking): possible hardcoded secrets.
    warnings = [
        f"possible {label}"
        for pat, label in SECRET_PATTERNS
        if re.search(pat, blob)
    ]
    if warnings:
        sys.stderr.write(
            f"WARNING (guard.py, non-blocking) in {path or 'content'}: "
            + "; ".join(warnings)
            + ". Move credentials to env/config; never commit secrets "
            "(.claude/rules/rules.md).\n"
        )
    sys.exit(0)


if __name__ == "__main__":
    main()
