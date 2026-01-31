---
name: kmp-testing
description: Guides the creation and organization of tests in Kotlin Multiplatform projects. Covers dispatcher setup, Mokkery mocking, test placement, and common KMP testing pitfalls.
---

# KMP Testing Skill

This skill provides best practices and solutions for common issues when writing tests in Kotlin Multiplatform projects.

## Critical Issues and Solutions

### 1. Dispatchers.Main Not Available in JVM Tests

**Problem:** Accessing `Dispatchers.Main` in JVM tests causes `IllegalStateException`:
```
Module with the Main dispatcher is missing. Add dependency providing the Main dispatcher
```

**Solutions:**

#### Option A: Use `UnconfinedTestDispatcher` (Recommended)
```kotlin
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class MyRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = CoroutineDispatchers(
        main = testDispatcher,
        mainImmediate = testDispatcher,
        io = testDispatcher,
        default = testDispatcher
    )
}
```

#### Option B: Use `StandardTestDispatcher` with Scheduler
```kotlin
import kotlinx.coroutines.test.StandardTestDispatcher

class MyRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = CoroutineDispatchers(
        main = testDispatcher,
        mainImmediate = testDispatcher,
        io = testDispatcher,
        default = testDispatcher
    )
    
    @Test
    fun myTest() = runTest {
        // Test code
    }
}
```

**Warning:** Never use default `CoroutineDispatchers()` constructor in tests - it tries to access `Dispatchers.Main`.

### 2. Test Dependencies Configuration

**Problem:** `runTest` or Mokkery not found, or tests fail with suspend function errors.

**Solution:** Ensure `commonTest` dependencies are properly configured:

```kotlin
// In module's build.gradle.kts
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.testing) // Contains kotlinx-coroutines-test, turbine
        }
    }
}
```

**Required dependencies in `libs.versions.toml`:**
```toml
[bundles]
testing = ["kotlinx-coroutines-test", "turbine", "compose-ui-test"]
```

### 3. Test Organization by Module

**Where to place tests:**

| Test Type                  | Location                                            | Module        |
| -------------------------- | --------------------------------------------------- | ------------- |
| Domain models              | `shared/core/src/commonTest/.../domain/models/`     | `shared:core` |
| Use cases                  | `shared/core/src/commonTest/.../domain/usecases/`   | `shared:core` |
| State machines             | `shared/core/src/commonTest/.../domain/`            | `shared:core` |
| Utility functions          | `shared/core/src/commonTest/.../utils/`             | `shared:core` |
| Repository implementations | `shared/data/src/commonTest/.../data/repositories/` | `shared:data` |
| DAOs (with in-memory DB)   | `shared/data/src/commonTest/.../data/local/`        | `shared:data` |
| UI Components              | `sharedUI/src/commonTest/.../ui/`                   | `sharedUI`    |

**Rule:** Test classes should live in the same module as the code they're testing.

### 4. Mokkery Mocking Patterns

**Setup in module:**
```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.mokkery)
}
```

**Common patterns:**

#### Mocking Repositories
```kotlin
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend

class MyUseCaseTest {
    private val repository = mock<MyRepository>()
    
    @Test
    fun myTest() = runTest {
        // Setup
        everySuspend { repository.getData(any()) } returns MyData()
        
        // Execute
        val result = useCase.invoke(123)
        
        // Verify
        verifySuspend { repository.getData(123) }
    }
}
```

#### Typed Matchers for Ambiguity
When the compiler can't infer types, use typed matchers:
```kotlin
everySuspend { dao.insert(any<MyEntity>()) } returns Unit
```

#### Non-suspend Functions
Use `every` instead of `everySuspend`:
```kotlin
every { dao.getFlow(any()) } returns flowOf(data)
```

### 5. runTest Usage

**Correct patterns:**
```kotlin
// Simple test
@Test
fun myTest() = runTest {
    // Test code with suspend calls
}

// With specific dispatcher (less common)
@Test
fun myTest() = runTest(testDispatcher) {
    // Test code
}
```

**Avoid:**
```kotlin
// Don't use StandardTestDispatcher without proper setup
private val dispatcher = StandardTestDispatcher() // Missing scheduler control

// Don't call suspend functions outside runTest
@Test
fun badTest() {
    repository.save() // Error: must be in coroutine
}
```

### 6. Flow Testing with Turbine

```kotlin
import app.cash.turbine.test

@Test
fun testFlow() = runTest {
    repository.observeData().test {
        assertEquals(expected, awaitItem())
        awaitComplete()
    }
}
```

### 7. Resource Testing

For tests using Compose resources:
```kotlin
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.my_string

@Test
fun testResourceUsage() {
    val comment = MatchComment(Res.string.my_string)
    assertEquals(Res.string.my_string, comment.res)
}
```

## Quick Checklist for New Tests

- [ ] Test file in correct module (`shared:core`, `shared:data`, or `sharedUI`)
- [ ] `commonTest.dependencies` includes `kotlin("test")` and testing bundle
- [ ] Mokkery plugin applied if using mocks
- [ ] Using `UnconfinedTestDispatcher` for any dispatcher needs
- [ ] All suspend calls inside `runTest { }`
- [ ] Using `everySuspend` for suspend functions, `every` for regular
- [ ] Using `verifySuspend` for suspend verification, `verify` for regular

## Common Errors and Fixes

### "Suspend function can only be called from a coroutine"
**Fix:** Wrap test body in `runTest { }` or make the lambda suspend

### "Dispatchers.Main accessed when platform dispatcher was absent"
**Fix:** Use `UnconfinedTestDispatcher` instead of default `CoroutineDispatchers()`

### "Unresolved reference: runTest"
**Fix:** Add `implementation(libs.kotlinx.coroutines.test)` to `commonTest.dependencies`

### "None of the following candidates is applicable: fun <T> MokkeryMatcherScope.any()"
**Fix:** Use typed matcher: `any<MyType>()` or ensure you're inside `everySuspend { }` block

### Tests pass but not counted in Kover coverage for module
**Fix:** Ensure tests are in the correct module's `src/commonTest` directory, not a different module

## Performance Tips

- Use `UnconfinedTestDispatcher` for faster tests (executes immediately)
- Use `StandardTestDispatcher` only when you need precise control over coroutine execution
- Avoid `Dispatchers.setMain()` in KMP tests - use constructor injection instead
