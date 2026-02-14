# 🧩 Memory-Match: Project Intelligence (GEMINI.md)

This document serves as the **Immutable Source of Truth** and instructional context for Gemini CLI and other AI agents interacting with the Memory-Match project.

## 🚀 Project Overview

**Memory-Match** is a premium, cross-platform memory game built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (1.10.0+)**. It leverages modern 2026 standards, focusing on clean architecture, high-performance state management, and an "AI-Agentic" development workflow.

### 🏗️ Architecture & Tech Stack

The project follows a **Local-First, Modular-Core** strategy:

- **UI Layer (`sharedUI`)**: Uses **Compose Multiplatform** for shared rendering and **Decompose** for lifecycle-aware navigation and component-based logic.
- **Core Layer (`shared:core`)**: The "Brain" of the app. Contains the **GameStateMachine** (managed via a custom DSL), domain models, and core business logic (Scoring, Time Attack).
- **Data Layer (`shared:data`)**: The "Memory". Implements **Room KMP** for local persistence and **Ktor** for networking.
- **Dependency Injection**: Powered by **Koin (4.1.1+)**, favoring **Context Parameters** for boilerplate-free injection.

### 🎮 Key Features
- **Heat System**: A combo-driven mechanic that triggers "Heat Mode" with visual transformations and haptic feedback.
- **Time Attack & Daily Challenge**: Distinct game modes with specialized logic.
- **Shop System**: Customization for card backs and skins.
- **Dynamic Dealer**: AI-driven commentary based on gameplay.
- **Robust Testing**: High coverage (90.9%+) using **Turbine** (Flows) and **Mokkery** (KSP-based mocking).

---

## 🛠️ Building and Running

Always prefer specific module tasks to avoid building all targets unnecessarily.

### 📦 Key Commands

| Task | Command |
| :--- | :--- |
| **Check Compilation** | `./gradlew :sharedUI:compileCommonMainKotlinMetadata` |
| **Run Android** | `./gradlew :androidApp:installDebug` |
| **Run Desktop** | `./gradlew :desktopApp:run` |
| **Run All Tests** | `./run_tests.sh` |
| **Linting (Check)** | `./gradlew detekt` |
| **Linting (Apply)** | `./gradlew spotlessApply` |
| **Coverage Report** | `./gradlew koverHtmlReport` |

---

## 📏 Development Conventions (2026 Standards)

Adhere strictly to these patterns to maintain code health and agent compatibility.

### 🚫 Prohibited Patterns ("The Kill List")
- **NO `viewModelScope`**: Use `componentScope` (Decompose-native).
- **NO `!!`**: Use `requireNotNull()` or safe calls `?.`.
- **NO Hardcoded Strings**: Always use `Res.string.key`.
- **NO Platform Leakage**: Keep `java.*` or `android.*` out of `commonMain`.
- **NO Logic in UI**: Move all decision-making to `Components` or `StateMachines`.

### ✅ Preferred Idioms
- **Context Parameters**: Use `context(Service)` for dependency access.
- **Immutable Collections**: Use `persistentListOf()`, `persistentMapOf()` for Compose state to enable **Strong Skipping**.
- **Conventional Commits**: Use `feat:`, `fix:`, `chore:`, `refactor:`, etc.
- **UDF (Unidirectional Data Flow)**: Components expose `StateFlow<State>` and handle `Intents`.

### 🤖 Agent Guidance
- **Source of Truth**: Refer to `AGENTS.md` and `.agent/rules/` for granular instructions.
- **Task Verification**: After changes, always run `:sharedUI:compileCommonMainKotlinMetadata` and relevant tests.
- **Refactoring**: When adding features, ensure they integrate with the `GameStateMachine` and follow existing DSL patterns.

---
*Last Updated: February 2026*
