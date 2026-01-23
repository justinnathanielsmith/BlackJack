---
trigger: glob
globs: ["**/*Test.kt"]
---

# 🧪 Testing Standards

## Stack
- **Turbine**: For Flow testing.
- **Mokkery**: KSP-based mocking.
- **Kermit**: Use `StaticConfig()` for logging in tests.

## Structure
Organize tests using `// region` blocks:
1. `Setup`
2. `Initial State`
3. `Intents`
4. `Navigation`

```kotlin
@Test
fun `example`() = runTest {
    val repository = mock<Repository>()
    everySuspend { repository.getData() } returns "Success"
    // ... test logic
}
```