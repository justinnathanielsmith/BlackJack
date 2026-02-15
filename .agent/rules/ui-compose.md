---
trigger: glob
globs: ["**/*UI.kt", "**/*Screen.kt", "**/*Component.kt", "**/*Content.kt", "**/ui/**/*.kt"]
---

# 🎨 Compose UI

## Layout & Architecture
- **Adaptive Layouts**: Always check `WindowSizeClass`.
- **Slot APIs**: Use `@Composable` lambdas for flexible content.
- **Modifiers**: The **first** optional parameter must be `modifier`.
- **Strong Skipping**: Use `PersistentList` and other immutable collections in UI `State` models to ensure stability and enable strong skipping.
- Maintain Unidirectional Data Flow (UDF).

## 🔊 UI Events (Audio, Haptics, Nav)
- **Immediate Feedback**: Trigger directly from Composable (on click).
- **Logic Result Events**: Trigger via `Channel(Channel.BUFFERED)` in `ScreenModel`.
- **Collection**: Use `LaunchedEffect(Unit)` in the Composable to collect events.