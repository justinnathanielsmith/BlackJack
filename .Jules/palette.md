# 🎨 Palette's Journal

## UX Learnings

### Accessibility
- **MedallionIcon**: Ensure all icon-only buttons have a `contentDescription`. The `StartContent.kt` screen had several icons (Shop, Stats, Settings) with `null` descriptions.
- **Compose Multiplatform**: Strings are accessed via `Res.string` generated from `strings.xml`. Ensure proper imports or usage of `Res.string`.
- **Selection Controls**: Use `Modifier.selectable` with `Role.RadioButton` (or `Role.Tab`) for segmented controls or custom radio groups instead of `clickable`. This ensures screen readers announce the selection state ("Selected") and the correct role.
- **PokerButton**: Added `Role.Button` and `contentDescription` parameter to `PokerButton` to improve accessibility. This ensures that custom buttons are correctly announced as buttons and can have custom labels.
- **Icon Buttons**: Standardize on `IconButton` for top bar and other icon-only actions. It enforces the minimum touch target size (48dp) even if the visual content is smaller (e.g., in compact mode).
    - Wrap custom visual content (like `Surface` or `Box`) inside `IconButton`.
    - Ensure `contentDescription` is provided.

### Decompose
- Remember to use `componentScope` for coroutines in components.

### Tactile Feedback (Haptics)
- **HapticService**: Inject `LocalAppGraph` to access `hapticsService`. Use `LIGHT` for standard button/selection interactions and `HEAVY` for primary actions (e.g., Starting the game) to provide distinct physical feedback.
- **Consistency**: Adding haptics to base UI components (`PokerButton`, `PokerChip`, `PillSegmentedControl`) ensures a consistent tactile experience across the entire app without cluttering high-level screen code.
- **Top Bar Actions**: Added `LIGHT` haptic feedback to Game Top Bar actions (Back, Restart, Mute) to improve interactivity.

### Wallet Accessibility
- **Semantic Grouping**: When a custom component like `WalletBadge` displays multiple pieces of info (icon + text), use `Modifier.semantics { contentDescription = "..." }` on the container to provide a clear, unified description for screen readers (e.g., "Wallet: 1,000").
