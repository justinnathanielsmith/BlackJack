---
trigger: glob
globs: ["**/*Test.kt"]
description: Mokkery Mocking Standards
---

# 🃏 Mokkery Mocking Standards

Mokkery is the primary mocking library for KMP in this project. Use it for all interfaces and classes that need to be mocked in tests.

## 1. Mock Creation
```kotlin
val service = mock<MyService>()
```

## 2. Stubbing (every)
Use `every` for regular functions and `everySuspend` for suspending functions.

```kotlin
// Regular
every { service.getData() } returns "Data"

// Suspend
everySuspend { service.fetchData() } returns "Remote Data"

// With Arguments
every { service.process(any()) } returns true
every { service.calculate(arg(it > 0)) } returns 1
```

## 3. Verification (verify)
Use `verify` and `verifySuspend` to assert interactions.

```kotlin
verify { service.getData() }
verifySuspend { service.fetchData() }

// Number of calls
verify(atLeast = 1) { service.process(any()) }
verify(atMost = 5) { service.process(any()) }
verify(exactly = 2) { service.process(any()) }
```

## 4. Argument Matchers
- `any()`: Matches any value.
- `arg(predicate)`: Matches based on a lambda.
- `eq(value)`: Matches equal to value.

## 5. Capturing Arguments
```kotlin
val captor = Slot<MyData>()
every { service.save(capture(captor)) } returns Unit

// ... run code ...

assertEquals("Expected", captor.value.name)
```
