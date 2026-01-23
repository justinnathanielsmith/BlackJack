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
3. **Context Parameters**: Use `-Xcontext-parameters`, NOT Context Receivers.
4. **Conventional Commits**: `feat:`, `fix:`, `chore:`, etc.

## 1. Tech Stack Overview
- **UI**: Compose Multiplatform 1.10+
- **DI**: Metro (Compiler Plugin)
- [ ] Nav: Decompose
- **DB**: Room (KMP)
- **Network**: Ktor 3.x
- **Testing**: Turbine + Mokkery

## 🚫 Prohibited Patterns (The "Kill List")
- `viewModelScope` -> Use `componentScope` (Decompose lifecycle scope).
- `java.*` / `android.*` in shared code -> Use `kotlinx.*` or `expect/actual`.
- Hardcoded Strings -> Use `Res.string.my_key`.
- `!!` -> Use `requireNotNull` or `?.`.
- Logic in UI -> Move to `Component` or `UseCase`.
- `ConstraintLayout` -> Use `Column`, `Row`, `Box`.