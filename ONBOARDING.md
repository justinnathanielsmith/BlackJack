# 🚀 Memory-Match Onboarding Guide

Welcome to the **Memory-Match** engineering team! This document is your comprehensive guide to getting started, understanding our architecture, and contributing effectively to the codebase.

## 🌟 Project Vision

**Memory-Match** is a high-performance, cross-platform memory game built with **Kotlin Multiplatform (KMP)**. More than just a game, it serves as a **reference implementation** for modern 2026 development standards, showcasing:

*   **Clean Architecture** & **Modular Design**
*   **AI-Agentic Development** workflows
*   **Compose Multiplatform** for shared UI
*   **Decompose** for robust state management

Our goal is to demonstrate how to build premium, production-quality apps that share 95%+ of code across Android, iOS, and Desktop.

---

## 🛠️ Prerequisites & Setup

Before you begin, ensure your development environment meets these requirements:

### Required Tools
*   **JDK 21**: We recommend [Azul Zulu](https://www.azul.com/downloads/?version=java-21-lts&package=jdk).
*   **Android Studio Ladybug** (or newer) / **IntelliJ IDEA 2024.3+**.
*   **Xcode 15.0+**: Required for building and running the iOS application.
*   **Cocoapods**: Required for dependency management on iOS.

### Initial Setup
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/smithjustinn/Memory-Match.git
    cd Memory-Match
    ```

2.  **Validate Environment**:
    Run the check task to ensure everything is configured correctly.
    ```bash
    ./gradlew check
    ```

3.  **Run the App**:
    *   **Android**: Select `androidApp` configuration in AS or run `./gradlew :androidApp:installDebug`
    *   **Desktop**: `./gradlew :desktopApp:run`
    *   **iOS**: Open `iosApp/iosApp.xcworkspace` in Xcode and run.

---

## 🏗️ Architecture Deep Dive

We follow a **Local-First, Modular-Core** strategy. The project is structured to maximize code sharing while allowing for platform-specific optimizations.

### Module Structure
*   **`:shared:core`**:
    *   **Purpose**: The heart of the application. Contains common utilities, base classes, and core domain logic.
    *   **Key Dependencies**: `kotlinx-coroutines`, `kotlinx-datetime`, `kermit` (logging).
*   **`:shared:data`**:
    *   **Purpose**: Data layer implementation. Handles persistence, networking, and repositories.
    *   **Key Tech**: `Room KMP` (Database), `Ktor` (Network).
*   **`:sharedUI`**:
    *   **Purpose**: The massive shared UI module. Contains all screens, components, and view models.
    *   **Key Tech**: `Compose Multiplatform`, `Decompose`, `Koin`.

### Component-Based MVVM (State Machine Pattern)
We use **Decompose** combined with a **State Machine** pattern to implement robust, testable UI logic.

1.  **Component (State Holder)**:
    *   Implements the `ComponentContext` interface.
    *   Holds an instance of a `GameStateMachine` (or similar) to manage complex transitions.
    *   Exposes `StateFlow<UIState>` for the UI to observe.
    *   Handles user intents by dispatching actions to the state machine.
    *   **Rule**: Use `componentScope` for coroutines, NOT `viewModelScope`.
2.  **UI (View)**:
    *   Pure `@Composable` functions.
    *   Observes state and dispatches events to the Component.
    *   **Rule**: UI should be dumb. No complex logic in Composables.

---

## ⚡ Tech Stack (2026 Standards)

We stay on the bleeding edge of the Kotlin ecosystem.

| Category       | Technology            | Version | Notes                         |
| :------------- | :-------------------- | :------ | :---------------------------- |
| **Language**   | Kotlin                | 2.3.0+  | K2 Mode enabled.              |
| **UI**         | Compose Multiplatform | 1.10.0+ | Use Strong Skipping mode.     |
| **DI**         | Koin                  | 4.1.1+  | Context Parameters favored.   |
| **Navigation** | Decompose             | 3.4.0+  | Platform-agnostic navigation. |
| **DB**         | Room KMP              | 2.8.4+  | Type-safe SQLite.             |
| **Images**     | Coil                  | 3.3.0+  | Shared image loading.         |
| **Animation**  | Compottie             | 2.0.2+  | Lottie for Compose KMP.       |
| **Testing**    | Turbine + Mokkery     | -       | Flow testing & KSP mocking.   |

---

## 📝 Development Protocols

To maintain high quality and velocity, please strictly adhere to these protocols.

### 1. Git & Commits
*   **Conventional Commits**: We use the [Conventional Commits](https://www.conventionalcommits.org/) specification.
    *   `feat: add new game mode`
    *   `fix: resolve crash on startup`
    *   `chore: update gradle dependencies`
    *   `refactor: simplify game logic`

### 2. Gradle Build Strategy
*   **Avoid** `assemble` on the root or shared modules, as it builds ALL targets (heavy).
*   **Preferred Commands**:
    *   Check compilation: `./gradlew :sharedUI:compileCommonMainKotlinMetadata` (Fast)
    *   Run Android: `./gradlew :androidApp:installDebug`
    *   Run Desktop: `./gradlew :desktopApp:run`

### 3. Static Analysis
We use **Spotless** for formatting and **Detekt** for code quality.
*   **Format Code**: `./gradlew spotlessApply`
*   **Run Check**: `./gradlew detekt`
*   **Auto-Correct**: `./gradlew detekt --auto-correct`

### 4. Testing
*   **Run All Tests**: `./run_tests.sh`
*   **Shared Logic**: `./gradlew :sharedUI:allTests`

---

## 🤖 Working with AI Agents

This repository is optimized for **AI-Agentic Development**.

*   **`AGENTS.md`**: This is the "Constitution" for AI agents. It contains the core rules and architectural constraints. If you change a core pattern, update this file.
*   **Agent Rules**: Specialized rules are located in `.agent/rules`.
*   **Context**: When working with an AI (like Antigravity), encourage it to read `AGENTS.md` first to align with project standards.

---

## 🚫 The "Kill List" (Prohibited Patterns)

*   ❌ `viewModelScope` -> ✅ Use `componentScope`
*   ❌ `java.*` / `android.*` in shared code -> ✅ Use `kotlinx.*` or `expect/actual`
*   ❌ Hardcoded Strings -> ✅ Use `Res.string.my_key`
*   ❌ `!!` (Null Assertions) -> ✅ Use `requireNotNull()` or `?.`
*   ❌ Mutable Collections in State -> ✅ Use `kotlinx.collections.immutable`
*   ❌ `ConstraintLayout` (in Compose) -> ✅ Use `Column`, `Row`, `Box` (better performance)
*   ❌ Logic in UI -> ✅ Move to `Component` or `StateMachine`

---

Welcome aboard! 🚀 If you have questions, check the `docs/` folder or ask in the team channel.
