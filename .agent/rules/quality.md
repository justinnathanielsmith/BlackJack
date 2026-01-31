---
trigger: model_decision
description: clean up the code by linting and checking for code smells
---

# Antigravity Quality Rules

- **Zero-Tolerance Policy:** All code must pass `./gradlew spotlessCheck` and `./gradlew detekt`.
- **Auto-Fixing:** If a task involves changing code, the agent MUST run `./gradlew spotlessApply` before reporting the task as complete.
- **Complexity Guard:** Any new function with a Cyclomatic Complexity > 10 (as reported by Detekt) must be refactored into smaller private functions immediately.
- **Formatting:** We use `ktfmt` (Google style). Do not use standard `ktlint` defaults.

**Context - Compose Rules:** We have suppressed `LongParameterList` and `FunctionNaming` for `@Composable` functions. If you encounter a naming issue in a UI component, refer to the `detekt.yml` before attempting to "fix" it. Do not attempt to refactor UI parameters into data classes unless they exceed 10 items.
