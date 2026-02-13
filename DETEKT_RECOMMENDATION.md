# Detekt Configuration Recommendation

## Issue
During the audit of `shared/core/src/commonMain/kotlin/io/github/smithjustinn/domain/MemoryGameLogic.kt`, I identified that the `handleMatchSuccess` function had grown to over 60 lines, making it difficult to maintain and test. This complexity was primarily due to mixing scoring logic, state updates, and side effects within a single function.

## Current Configuration
The current `detekt.yml` does not explicitly set a strict threshold for `LongMethod`, relying on default values (which are typically 60 lines) or `CyclomaticComplexMethod`.

## Recommendation
I recommend configuring the `LongMethod` rule to be stricter for non-Compose code (domain logic) while maintaining leniency for UI code.

### Proposed `detekt.yml` Snippet

```yaml
complexity:
  LongMethod:
    active: true
    threshold: 40  # Stricter limit for domain logic (default is 60)
    ignoreAnnotated:
      - 'androidx.compose.runtime.Composable' # Compose functions often exceed this due to layout hierarchy
```

## Rationale
- **Domain Logic**: Functions in the domain layer (like `MemoryGameLogic`) should be small, focused, and testable. A 40-line limit encourages extracting helper functions (e.g., `createMatchSuccessState`) and separating concerns.
- **Compose UI**: UI code naturally tends to be longer due to declarative structure and nesting. Ignoring `@Composable` functions prevents false positives while keeping business logic clean.

## Action Taken
Refactored `MemoryGameLogic.kt` to extract `createMatchSuccessState` and encapsulating constants, reducing `handleMatchSuccess` complexity significantly.
