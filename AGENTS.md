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
Use the Memory-Match CLI:
```bash
./mm.py test all
```

Or specific modules:
* Shared: `./mm.py test shared`
* Android: `./mm.py test android`
* Desktop: `./mm.py test desktop`

### 🏗 Building & Running
* Run Android: `./mm.py build android --run`
* Run Desktop: `./mm.py build desktop --run`
* Compile Metadata: `./mm.py build metadata`

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

---

## 🛠️ 5. Memory-Match CLI (mm.py)

The `mm.py` Python script is the unified entry point for project operations.

### 📦 Key Commands
- **Task Management**: `./mm.py task new <type> <name>`
  - Creates a new worktree and branch.
  - Syncs config files automatically.
- **AI Context**: `./mm.py context`
  - Generates a high-signal summary of changes and impacted modules.
- **Finalize Task**: `./mm.py done`
  - Runs linting/formatting and suggests a commit message.
- **Sync**: `./mm.py sync`
  - Syncs config files across all workspaces.


[def]: .agent/rules/