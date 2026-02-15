---
trigger: glob
globs: ["**/*.kt"]
---

# 📏 Kotlin 2.3 Idioms

All Kotlin code must adhere to 2026 standards.

## 🚫 Imports

- **No Wildcard Imports**: Never use wildcard imports (`import package.*`). Always use explicit imports for clarity and to prevent namespace pollution.
- **No Fully Qualified Names (FQNs)**: Never use FQNs in code. Always import the type and use its simple name. If there is a name collision, use an alias (`import package.Type as TypeAlias`).

## 🛠 Preferred Idioms (2026)

- **Expressive Guarding**: Use `takeIf` and `takeUnless` for concise validation.
- **Pattern Matching**: Exhaustive `when` blocks are mandatory for sealed classes/interfaces.
- **Iteration**: Prefer `forEach` or `onEach` for side-effect-heavy loops.
- **Immutability**: Always prefer `PersistentList`, `PersistentMap`, and `PersistentSet` from `kotlinx.collections.immutable` for state models.

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
```kotlin
val json = $$"""
{
  "key": "${value}"
}
"""$$
```

## 🕒 Time & Clock (Kotlin 2.3 Stable)

Kotlin 2.3 stabilized `kotlin.time.Clock` and `kotlin.time.Instant`. Standardize on these built-in APIs instead of `kotlinx-datetime` for common tasks.

### Clock and Instant Handling
- **Prefer Standard Library**: Use `kotlin.time.Clock.System` for wall-clock time and `TimeSource.Monotonic` for measuring duration.
- **Inject for Testability**: Always inject `Clock` or `TimeSource` via constructor parameters or DI to enable deterministic testing. 
- **Avoid Long for Time**: Use `kotlin.time.Duration` for intervals instead of raw `Long` (milliseconds/seconds).

```kotlin
// ✅ DO - Injected Clock and Duration usage
class SessionManager(private val clock: Clock) {
    fun isExpired(startTime: Instant, timeout: Duration): Boolean {
        return (clock.now() - startTime) > timeout
    }
}

// ❌ DON'T - Static access and raw Longs
fun isExpired(startMillis: Long): Boolean {
    return (System.currentTimeMillis() - startMillis) > 3600000 
}
```