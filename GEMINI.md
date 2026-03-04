# 🧠 Memory-Match: Architecture & Intelligence (GEMINI.md)

This document is the **Pure Source of Truth** for the "Brain" of Memory-Match. 
For CLI usage and routing, see **[AGENTS.md](AGENTS.md)**.
For coding standards, see **[Rules Index (.agent/rules/INDEX.md)](.agent/rules/INDEX.md)**.

---

## 🚀 1. Architecture Overview (Local-First)

Memory-Match follows a **Modular-Core** strategy:

- **UI Layer (`sharedUI`)**: Compose Multiplatform + Decompose (lifecycle-aware).
- **Core Layer (`shared:core`)**: The "Brain". Custom DSL for `GameStateMachine`.
- **Data Layer (`shared:data`)**: The "Memory". Room KMP (local persistence).
- **DI**: Koin 4.1.1+ (favoring **Context Parameters**).

---

## 🧪 2. GameStateMachine DSL (`:shared:core`)

The logic is expressed via a custom DSL:
- **State Alignment**: Every UI event maps to a `GameIntent`.
- **Immutable Reducers**: Atomic state updates for consistency.
- **Heat Logic**: Managed within the state machine for visuals/haptics.

---

## 🤖 3. Agent Intelligence Guide

*   **Logic Refactoring**: Analyze `:shared:core` and the `StateMachine` implementation.
*   **Feature Creation**: Follow the **[Feature Creation Checklist](.agent/rules/feature-creation.md)**.
*   **Verification**: Always use `./mm.py done` to verify formatting and linting.

---
*Last Updated: March 2026*
