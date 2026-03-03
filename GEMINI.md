# 🧩 Memory-Match: Project Intelligence (GEMINI.md)

This document serves as the **Pure Source of Truth** for the "Brain" and Architecture of the Memory-Match project. For CLI usage and general routing, see **[AGENTS.md](file:///Users/justinsmith/Projects/memory-match/work/AGENTS.md)**.

---

## 🚀 1. Architecture Overview

Memory-Match follows a **Local-First, Modular-Core** strategy:

- **UI Layer (`sharedUI`)**: Uses **Compose Multiplatform** and **Decompose** for lifecycle-aware navigation.
- **Core Layer (`shared:core`)**: The "Brain". Contains the **GameStateMachine** (managed via a custom DSL).
- **Data Layer (`shared:data`)**: The "Memory". Implements **Room KMP** for local persistence.
- **Dependency Injection**: Powered by **Koin (4.1.1+)**, favoring **Context Parameters**.

---

## 🧠 2. GameStateMachine DSL

The core game logic is expressed via a custom DSL located in `:shared:core`.

### 🎛️ Key Patterns
*   **State Alignment**: Every UI event must map to a `GameIntent`.
*   **Immutable Reducers**: The state machine uses atomic updates to ensure consistency.
*   **Heat Logic**: Managed entirely within the state machine to trigger visual/haptic effects.

---

## 📏 3. Development Conventions (Soul)

### ✅ Preferred Idioms
- **Context Parameters**: Use `context(Service)` for dependency access.
- **Immutable Collections**: Use `persistentListOf()`, `persistentMapOf()` for Compose state.
- **UDF (Unidirectional Data Flow)**: Components expose `StateFlow<State>` and handle `Intents`.

### 🚫 Prohibited Patterns ("The Kill List")
- **NO `viewModelScope`**: Use `componentScope`.
- **NO Platform Leakage**: Keep `java.*` or `android.*` out of `commonMain`.
- **NO Logic in UI**: Move decision-making to `Components` or `StateMachines`.

---

## 🤖 4. Agent Guidance: High-Intelligence Research

*   **Deep Dive**: If you are refactoring logic, spend time in `:shared:core` reading the `StateMachine` implementation.
*   **Integration**: When adding features, ensure they follow the **[Feature Creation Checklist](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/feature-creation.md)**.
*   **Routing**: For build/test commands, refer to **[AGENTS.md](file:///Users/justinsmith/Projects/memory-match/work/AGENTS.md)**.

---
*Last Updated: March 2026*
