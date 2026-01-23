---
trigger: glob
globs: ["**/*.kt"]
---

# 📏 Kotlin 2.3 Idioms

All Kotlin code must adhere to 2026 standards.

## Context Parameters
```kotlin
// ✅ DO
context(logger: Logger)
fun logError(msg: String) { logger.error(msg) }

// ❌ DON'T
context(Logger) fun logError(...) 
```

## Guard Conditions
```kotlin
when (val response = api.get()) {
    is Success if response.data.isEmpty() -> showEmptyState()
    is Success -> showContent(response.data)
}
```

## Multi-Dollar Strings
Use `$$` for JSON or Regex strings to avoid escaping curly braces.