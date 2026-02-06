# 🎨 Palette's Journal

## UX Learnings

### Accessibility
- **MedallionIcon**: Ensure all icon-only buttons have a `contentDescription`. The `StartContent.kt` screen had several icons (Shop, Stats, Settings) with `null` descriptions.
- **Compose Multiplatform**: Strings are accessed via `Res.string` generated from `strings.xml`. Ensure proper imports or usage of `Res.string`.
- **Selection Controls**: Use `Modifier.selectable` with `Role.RadioButton` (or `Role.Tab`) for segmented controls or custom radio groups instead of `clickable`. This ensures screen readers announce the selection state ("Selected") and the correct role.

### Decompose
- Remember to use `componentScope` for coroutines in components.
