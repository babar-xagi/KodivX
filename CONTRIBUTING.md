# Contributing to KodivX

Thank you for your interest in contributing to **KodivX**!  
Our goal is to build a fast, portable, type-safe analytical computing engine for Kotlin across JVM, Android, Native, and Web targets.

---

## 🎯 Code of Conduct

This project and everyone participating in it is governed by the [KodivX Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## 🏗️ Architectural Principles Every Contributor Must Know

Before submitting code, please review our core architectural tenets:

1. **Simple things must be simple:** Public APIs must be discoverable and concise without requiring execution contexts or memory allocators.
2. **Never box primitive columns:** Primitive columns must be backed by contiguous buffers/arrays, not `Array<Double?>` or `List<T>`.
3. **Multiplatform-First:** Code in `commonMain` must compile and behave identically across JVM, Android, Linux, macOS, Windows, iOS, JS, and Wasm.
4. **No unmeasured performance claims:** Any PR claiming a performance improvement must include reproducible JMH or platform benchmark evidence.
5. **Zero-cost abstractions:** Avoid virtual calls (`interface.get(i)`) in tight inner numerical loops.

---

## 🛠️ Development Setup

### Prerequisites
- **JDK 21** or later (Eclipse Adoptium Temurin 21 recommended).
- Git.
- Optional: [Qutivex](https://github.com/babar-xagi/Qutivex) package manager.

### Building & Running Tests
Clone the repository and run:

```powershell
# Verify all multi-module projects
./gradlew projects

# Run tests across all targets
./gradlew check

# Run the sample JVM CLI application
./gradlew :samples:jvm-cli:run
```

---

## 🌿 Branching & Pull Request Process

1. **Fork & Branch:** Create a feature branch with a descriptive name:
   ```text
   git checkout -b feature/validity-bitmap-swar
   ```
2. **Follow Code Style:** Adhere to `.editorconfig` and official Kotlin guidelines (`kotlin.code.style=official`).
3. **Write Tests:** Every new feature, kernel, or operator must include:
   - Unit tests covering edge cases.
   - Null handling tests.
   - Cross-platform consistency tests.
4. **Document Decisions:** For significant architectural modifications, submit an Architecture Decision Record (ADR) in `docs/adr/`.
5. **Open a PR:** Ensure all checks pass and describe the problem solved, design choices, and benchmark impact.

---

## 📑 Area Labels for Issues and PRs

When opening issues or PRs, use the relevant area labels:
- `area:core`
- `area:buffer`
- `area:array`
- `area:frame`
- `area:expr`
- `area:query`
- `area:io`
- `area:sql`
- `area:arrow`
- `platform:jvm`
- `platform:native`
- `platform:wasm`
- `platform:js`
- `performance`
- `documentation`
