# 🤖 AGENTS.md: Project Dashboard

This document provides a high-level overview for AI agents and human developers working on the Memory-Match project.

> [!IMPORTANT]
> Detailed architectural rules, coding standards, and prohibited patterns have been moved to the [**.agent/rules/**][def] directory. Always refer to these specialized files for implementation specifics.

---

## 📋 1. Feature Checklist

When generating a new feature, follow the [**Feature Creation Checklist**][feature-creation].

[feature-creation]: .agent/rules/feature-creation.md

---

## 🏗️ 2. Build & Test Commands

### 🧪 Running Tests
Use the provided helper script:
```bash
./run_tests.sh
```

Or specific modules:
* Shared: `./gradlew :sharedUI:allTests`
* Android: `./gradlew :androidApp:testDebugUnitTest`
* Desktop: `./gradlew :desktopApp:test`

### 🏗 Building & Running
* Build All: `./gradlew build`
* Run Android: `./gradlew :androidApp:installDebug`
* Run Desktop: `./gradlew :desktopApp:run`

---

## 💡 3. Key Idioms & Standards (2026)

*   **DI**: Koin 4.1.1+ (via `AppGraph` facade for UI).
*   **State**: Use `PersistentList` / `PersistentMap` for UI State.
*   **Logic**: Prefer `takeIf`, `when` (exhaustive), and `forEach` idioms.
*   **DI Access**: Use `context(Service)` for dependency access in domain logic.
*   **Navigation**: Decompose with `componentScope`.

---

## 🧹 4. Maintenance
* Clean: `./gradlew clean`
* Refresh Dependencies: `./gradlew build --refresh-dependencies`


[def]: .agent/rules/