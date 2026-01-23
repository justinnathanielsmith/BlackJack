---
trigger: glob
globs: ["**/*.kt"]
---

# 🎨 Compose UI & Voyager

## Layout & Architecture
- **Adaptive Layouts**: Always check `WindowSizeClass`.
- **Slot APIs**: Use `@Composable` lambdas for flexible content.
- **Modifiers**: The **first** optional parameter must be `modifier`.

## Voyager ScreenModels
- Use `screenModelScope` for coroutines.
- Maintain Unidirectional Data Flow (UDF).

## 🔊 UI Events (Audio, Haptics, Nav)
- **Immediate Feedback**: Trigger directly from Composable (on click).
- **Logic Result Events**: Trigger via `Channel(Channel.BUFFERED)` in `ScreenModel`.
- **Collection**: Use `LaunchedEffect(Unit)` in the Composable to collect events.