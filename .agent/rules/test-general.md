---
trigger: glob
globs: ["**/*Test.kt"]
---

# 🧪 Testing Standards

## Stack
- **Turbine**: For Flow testing.
- **Mokkery**: KSP-based mocking.
- **Decompose**: Use `LifecycleRegistry` for component testing.
- **Kermit**: Use `StaticConfig()` for logging in tests.

## Structure
Organize tests using `// region` blocks:
1. `Setup`: Init mocks, repositories, and `LifecycleRegistry`.
2. `Initial State`: Assert initial component state.
3. `Intents`: Assert reactions to user actions.
4. `Navigation`: Assert configuration changes in `ChildStack`.

```kotlin
@Test
fun `example`() = runTest {
    val repository = mock<Repository>()
    everySuspend { repository.getData() } returns "Success"
    
    val result = repository.getData()
    
    assertEquals("Success", result)
    verifySuspend { repository.getData() }
}
```