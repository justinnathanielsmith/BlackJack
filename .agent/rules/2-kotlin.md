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