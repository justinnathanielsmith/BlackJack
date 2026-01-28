---
trigger: always_on
globs: ["**/*"]
---

# ⚡ General Project Protocol

You are an expert Android/KMP developer in **January 2026**.
Use this document as the **Immutable Source of Truth** for high-level project alignment.


## 0. Critical Handshake
Before generating code, you **MUST** align with:
1. **Kotlin Version**: Assume Kotlin **2.3+ (K2 Mode)**.
2. **UI Stack**: Compose Multiplatform **1.10.0+**.
3. **Conventional Commits**: `feat:`, `fix:`, `chore:`, etc.
4. **Build Strategy**: NEVER run `:sharedUI:assemble`. Use specific tasks (see below).

## ⚡ Gradle Build Protocol (Speed Optimization)
- **Do NOT** run `assemble` on KMP modules (it builds iOS x64/arm64/sim + Android + Desktop).
- **Prohibited**: `./gradlew :sharedUI:assemble`
- **Preferred**:
    - **Check Compilation**: `./gradlew :sharedUI:compileCommonMainKotlinMetadata` (Fastest ~10s)
    - **Check Desktop**: `./gradlew :sharedUI:compileKotlinJvm`
    - **Check Android**: `./gradlew :sharedUI:compileDebugKotlinAndroid` (if available) or `:androidApp:assembleDebug`

## 1. Tech Stack Overview
- **UI**: Compose Multiplatform 1.10+
- **DI**: Koin
- **Nav**: Decompose
- **DB**: Room (KMP)
- **Network**: Ktor 3.x
- **Testing**: Turbine + Mokkery

## 2. Compose Best Practices (2026)
- **Strong Skipping**: Defaults are good; minimize `@Stable` boilerplate.
- **Collections**: Use `kotlinx.collections.immutable`.
- **Modifiers**: Prefer `Modifier.Node` over `composed {}`.
- **Slots**: Use Slot API over passing complex data models.
- **State**: Defer state reads to Layout/Draw phases.

## 🚫 Prohibited Patterns (The "Kill List")
- `viewModelScope` -> Use `componentScope` (Decompose lifecycle scope).
- `java.*` / `android.*` in shared code -> Use `kotlinx.*` or `expect/actual`.
- `expect/actual` for complex logic -> Use `interface` + DI instead.
- Hardcoded Strings -> Use `Res.string.my_key`.
- `!!` -> Use `requireNotNull` or `?.`.
- Logic in UI -> Move to `Component` or `UseCase`.
- `ConstraintLayout` -> Use `Column`, `Row`, `Box`.