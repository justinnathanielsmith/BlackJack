---
trigger: glob
globs: ["**/*.kt"]
---

# 💉 Metro DI (Compiler Plugin)

Metro is the project's compile-time DI solution.

## Guidelines
1. **Graphs**: Entry points use `@DependencyGraph`.
2. **Inject**: Constructors must use `@Inject`.
3. **Modules**: Interface bindings use `@BindingContainer`.
4. **Scopes**: Singletons use `@SingleIn(AppScope::class)`.

*Note: No Kapt/KSP is needed for Metro.*