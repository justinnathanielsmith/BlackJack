# 🤖 Agent Best Practices: Memory-Match

To be an elite agent in this repository, follow these best practices for context management and workflow.

---

## 🧭 1. Follow Progressive Disclosure

Do not attempt to ingest the entire codebase. **Drill down only when needed.**

1.  **Start at [AGENTS.md](file:///Users/justinsmith/Projects/memory-match/work/AGENTS.md)** to understand the project soul and available tools.
2.  **Run `./mm.py context`** to get the current signal on what has changed or what you are working on.
3.  **Consult the [Rules Index](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/INDEX.md)** to find the exact standard for your current layer (UI, Data, Logic).
4.  **Check [GEMINI.md](file:///Users/justinsmith/Projects/memory-match/work/GEMINI.md)** only if you need to understand core state machines or complex DSLs.

---

## 🛠️ 2. Leverage the CLI (`mm.py`)

The CLI is your "force multiplier". **Always use it.**

*   **Task Isolation**: Use `./mm.py task new` to create a dedicated worktree. This prevents context pollution from previous tasks.
*   **Context Generation**: Use `./mm.py context` to bootstrap your understanding of a task. It highlights impacted modules and recent history.
*   **Quality Gates**: Never ask the user "is this okay?" before running `./mm.py done`. This command runs linting and formatting automatically.

---

## 📏 3. Maintain the Context

When you add a new feature:
1.  Update the **[Feature Creation Checklist](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/feature-creation.md)** if you find a new pattern.
2.  If you create a new rule file, add it to the **[Rules Index](file:///Users/justinsmith/Projects/memory-match/work/.agent/rules/INDEX.md)**.
3.  Keep `GEMINI.md` focused on "High Intelligence" (Architecture/DSLs) and avoid adding low-level build commands there.

---

## 🚫 4. Common Pitfalls to Avoid

*   **Platform Leakage**: Ensure no `android.*` or `java.*` imports end up in `commonMain`.
*   **DI Over-complexity**: Use `context(Service)` for simple dependency access. Avoid complicated constructor injection where possible.
*   **Ignoring Kover**: Keep coverage above **80%**. Run `./mm.py coverage` to verify.

---
*Be Proactive. Be Agentic. Be Antigravity.*
