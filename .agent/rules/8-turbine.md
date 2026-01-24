---
trigger: glob
globs: ["**/*Test.kt"]
---

# 🚀 Turbine Flow Testing Standards

Use **Turbine** for all `Flow`, `StateFlow`, and `Channel` assertions. 

## 1. StateFlow Pattern
Assertions on `StateFlow` should account for the immediate emission of the initial state.

- Use `awaitItem()` to capture consecutive states.
- Use `expectNoEvents()` after the final expected state to ensure stability.
- Use `expectMostRecentItem()` for a "fire and forget" check on the latest state if intermediate ones are irrelevant.

```kotlin
component.state.test {
    assertEquals(InitialState, awaitItem()) // Initial
    component.doAction()
    assertEquals(NewState, awaitItem())    // Reaction
}
```

## 2. Event/Channel Pattern
For one-shot events exported as flows, assert specific emissions.

```kotlin
component.events.test {
    component.onBack()
    assertTrue(awaitItem() is UiEvent.Back)
}
```

## 3. Time Manipulation
Always use `testScheduler` when testing timers or delays.

- Wrap test in `runTest`.
- Call `testScheduler.advanceTimeBy(ms)`.
- Use `testScheduler.runCurrent()` to trigger pending emissions before `awaitItem()`.

## 4. Best Practices
- **Clean up**: Use `cancelAndIgnoreRemainingEvents()` for long-running or hot flows.
- **Isolation**: Inject `StandardTestDispatcher` via `CoroutineDispatchers` to ensure deterministic execution.
- **Granularity**: Test one logical flow or side-effect per `test {}` block.
