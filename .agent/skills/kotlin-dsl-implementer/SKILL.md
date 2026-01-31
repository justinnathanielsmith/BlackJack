---
name: kotlin-dsl-implementer
description: Guides the implementation of Domain-Specific Languages (DSLs) in Kotlin using lambdas with receivers, @DslMarker, and performance-optimized builders.
---

# Kotlin DSL Implementer Skill

This skill provides a structured approach to designing and implementing expressive, type-safe DSLs in Kotlin.

## Workflow

### 1. Identification
Identify code that is repetitive, highly nested, or involves complex configurations/state transitions.
- **Candidates**: Configuration builders, State Machine transitions, UI component composition, Test data setup.

### 2. Design the Syntax
Draft how the DSL should look from the user's perspective.
- Use **Lambdas with Receiver** to achieve a nested structure.
- Use **Extension Functions** to integrate with existing types.
- Aim for a **Declarative** style (describe *what*, not *how*).

### 3. Implementation (Infrastructure)
1. **Define `@DslMarker`**: Create a custom annotation to handle scoping.
   ```kotlin
   @DslMarker
   annotation class MyDslMarker
   ```
2. **Create Builders**: Use classes annotated with your DSL marker to host the DSL logic.
3. **Top-level Entry Point**: Create an `inline` function that accepts a lambda with the builder as the receiver.

### 4. Implementation (Logic)
- Implement `transition`, `configure`, or `apply` methods within the builder.
- Use `mutableListOf` or `mutableMapOf` internally to collect configuration, then "freeze" it into an immutable result in the `build()` method.

### 5. Optimization
- Use `inline` for the entry-point function to avoid lambda object overhead.
- Ensure builders are lightweight.

### 6. Verification
- Verify that the DSL enforces scope correctly (e.g., trying to call a builder method from an outer scope should fail).
- Run unit tests to ensure functional parity with the original logic.

## Examples
See [GameStateMachineDSL.kt](file:///Users/justinsmith/Projects/BlackJack/shared/core/src/commonMain/kotlin/io/github/smithjustinn/domain/GameStateMachineDSL.kt) for a real-world implementation.
