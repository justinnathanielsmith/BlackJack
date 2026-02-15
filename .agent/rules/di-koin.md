---
trigger: glob
globs: ["**/*.kt"]
---

# 💉 Koin DI & AppGraph Pattern

The project uses **Koin (4.1.1+)** for dependency injection, wrapped in an **AppGraph** facade for the UI layer.

## Guidelines
1. **Modules**: Define dependencies in Koin `module { ... }` blocks (e.g., `CoreModule.kt`, `DataModule.kt`).
2. **AppGraph Facade**:
   - The UI layer (Decompose Components) accesses dependencies through the `AppGraph` interface.
   - `KoinAppGraph` implements `AppGraph` and uses `KoinComponent` to inject dependencies.
3. **Component Access**:
   - Pass `AppGraph` to Decompose Components during initialization.
   - Use `LocalAppGraph` in Composables to access the graph via `StaticCompositionLocal`.
4. **Context Parameters**:
   - Use `context(Service)` in domain logic/use cases for clean dependency access without explicit injection.

## Example (AppGraph Usage)
```kotlin
class DefaultGameComponent(
    private val appGraph: AppGraph,
    componentContext: ComponentContext,
) : GameComponent, ComponentContext by componentContext {
    private val gameStateRepository = appGraph.gameStateRepository
    // ...
}
```

*Note: Always update the `AppGraph` interface and `KoinAppGraph` implementation when adding new shared services or use cases.*
