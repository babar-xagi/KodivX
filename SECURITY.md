# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |

---

## Reporting a Vulnerability

The KodivX team takes security seriously. Data processing engines parse untrusted data sources (CSV, JSON, Parquet, network streams, and user queries), making memory safety and parser robustness vital.

If you discover a security vulnerability, please do **not** open a public issue on GitHub. Instead, report it responsibly:

- Send an email to the core maintainers: `security@kodivx.io` (or maintainer contact listed in project settings).
- Include detailed steps to reproduce the vulnerability, along with sample datasets or payloads if applicable.
- We will acknowledge receipt within 48 hours and work with you on a coordinated disclosure.

---

## Defensive Engineering Invariants in KodivX

KodivX is designed with explicit defenses against:
1. **Unbounded Allocations & Memory Exhaustion:** Defending against malformed headers or corrupt metadata declaring billions of rows/columns.
2. **Integer Overflow in Sizing Calculations:** Preventing buffer overflow or out-of-bounds access when multiplying dimensions/strides.
3. **Decompression & Parsing Bombs:** Safeguards against pathological nesting and decompression bombs in file I/O.
4. **Injection Safety in Query Systems:** SQL and filter expressions must enforce parameter binding rather than string concatenation.
