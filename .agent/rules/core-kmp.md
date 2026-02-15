---
trigger: glob
globs: ["**/*.kt"]
---

# 🌍 Kotlin Multiplatform (KMP) Best Practices (2026)

## 1. Source Set Discipline
- **Default to `commonMain`**: 95% of your code should live here. Only dropping to platform-specific source sets when absolutely necessary.
- **Platform Modules**: If you find yourself writing logic in `androidMain` or `iosMain`, pause and ask: "Can this be common using a library or abstraction?"

## 2. Abstraction Strategy
- **Interfaces + DI > Expect/Actual**:
    - **Prefer**: Defining an `interface` in `commonMain` and injecting the implementation via Koin DI.
    - **Avoid**: Logic-heavy `expect` classes which are hard to test and maintain.
    - **Use Expect/Actual Only For**: Simple platform bridges (e.g. `expect fun getPlatformName(): String`).

## 3. Architecture components
- **ViewModels**: MUST be in `commonMain`. No platform-specific ViewModels.
- **Resource Access**: Always use `composeResources` (e.g. `Res.string.key`, `Res.drawable.icon`). Do not use `R.id` or `UIImage` directly in shared code.
