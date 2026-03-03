# 🤖 AGENTS.md: Project Soul & Master Router

Welcome, Agent. This is your **Primary Entry Point** for the Memory-Match project. 

Memory-Match is a premium, AI-native KMP game built to 2026 standards. To work effectively, follow the **Progressive Disclosure** path below.

---

## 🧭 1. Context Map (Progressive Disclosure)

Do not attempt to read everything at once. Use this map to drill down into the context you need:

1.  **[Project Soul (AGENTS.md)](file:///Users/justinsmith/Projects/memory-match/work/AGENTS.md)**: You are here. High-level routing and CLI commands.
2.  **[Architecture & DSLs (GEMINI.md)](file:///Users/justinsmith/Projects/memory-match/work/GEMINI.md)**: The "Brain". Deep dive into `GameStateMachine`, custom DSLs, and core architecture.
3.  **[Rules Index (.agent/rules/INDEX.md)](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/INDEX.md)**: Categorized coding standards, prohibited patterns, and platform-specific rules.
4.  **[Agent Best Practices (.agent/BEST_PRACTICES.md)](file:///Users/justinsmith/Projects/memory-match/work/.agent/BEST_PRACTICES.md)**: How to behave, use the CLI, and maintain high-signal context.

---

## 🛠️ 2. Core Tooling: Memory-Match CLI (`mm.py`)

The `mm.py` script is your unified interface. **Always prefer these commands over raw Gradle.**

### 📦 Task Lifecycle
*   **Start Work**: `./mm.py task new <type> <name>` (Creates worktree/branch).
*   **Get Context**: `./mm.py context` (Generates high-signal summary of changes).
*   **Verify & Format**: `./mm.py done` (Runs spotless/detekt, suggests commit msg).
*   **Sync Configs**: `./mm.py sync` (Syncs `.env`/`local.properties` across worktrees).

### 🏗 Build & Run
*   **Android**: `./mm.py build android --run`
*   **Desktop**: `./mm.py build desktop --run`
*   **Metadata**: `./mm.py build metadata` (Fastest compilation check).
*   **Coverage**: `./mm.py coverage --open` (Check Kover reports).

### 🧪 Testing
*   **All**: `./mm.py test all`
*   **Specific**: `./mm.py test [shared|android|desktop]`

---

## 📏 3. Quick Idioms (2026)

*   **DI**: Koin 4.1.1+ (Context Parameters via `context(Service)`).
*   **UI State**: `kotlinx.collections.immutable` (PersistentList/Map).
*   **Lifecycle**: Decompose `componentScope` (NO `viewModelScope`).
*   **Logic**: Exhaustive `when`, `persistentListOf()`, `UDF` only.

---

## 🚀 4. Getting Started
If you are new to a task:
1.  Run `./mm.py context` to see what changed.
2.  Check **[Feature Creation Checklist](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/feature-creation.md)** if adding new functionality.
3.  Consult the **[Rules Index](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/INDEX.md)** for specific layer requirements (UI, Data, Test).

---
*Immutable Source of Truth | Last Updated: March 2026*