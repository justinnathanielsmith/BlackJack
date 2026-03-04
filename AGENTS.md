# 🤖 AGENTS.md: Project Master Router

This is your **Primary Entry Point**. 
Memory-Match follows **Progressive Disclosure** to keep context lean.

---

## 🧭 1. Context Map

1.  **[Architecture & DSLs (GEMINI.md)](GEMINI.md)**: The "Brain". Deep dive into logic and structure.
2.  **[Rules Index (.agent/rules/INDEX.md)](.agent/rules/INDEX.md)**: Full directory of coding standards.
3.  **[Agent Best Practices (.agent/BEST_PRACTICES.md)](.agent/BEST_PRACTICES.md)**: Behaving like a 2026 expert.

---

## 🛠️ 2. Memory-Match CLI (`mm.py`)

**Always prefer these commands over raw Gradle.**

-   **`./mm.py task new <type> <name>`**: Start a new task.
-   **`./mm.py context`**: Generate high-signal summary of changes.
-   **`./mm.py done`**: Verify, format, and prepare commit message.
-   **`./mm.py build [android|desktop|metadata]`**: Efficiently check compilation.
-   **`./mm.py test [all|shared|android|desktop]`**: Run specialized test suites.

---

## 📏 3. Quick Reference

-   **DI**: Koin 4.1.1+ (Context Parameters).
-   **UI**: Compose Multiplatform + Decompose (persistent collections).
-   **Checklist**: **[Feature Creation (.agent/rules/feature-creation.md)](.agent/rules/feature-creation.md)**.

---
*Immutable Source of Truth | Last Updated: March 2026*
