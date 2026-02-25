# UX & Code Recommendations

## The "Quick Win": Typewriter Effect for Dealer Commentary
**Currently:** The dealer's comments ("River Magic!", "Pot Odds!") appear instantly. This feels static and robotic.
**Recommendation:** Implement a simple "Typewriter Effect" where characters appear one by one. This adds personality and "juice" to the game, making the dealer feel like a character rather than just a text box.

## The "UX Friction" Report
### Desktop: Missing Tooltips & Focus
- **Tooltips:** Key navigation buttons (Back, Restart, Mute) lack tooltips. Mouse users must guess the function or rely on icons, which can be ambiguous (e.g., "Restart" vs "Refresh").
- **Recommendation:** Wrap `IconButton` in `TooltipBox` (available in Compose Multiplatform Material3) to provide clarity on hover.

### iOS: Non-Native Back Arrow
- **Iconography:** The Back Button uses the standard Material Design arrow (with tail). On iOS, users expect a simple chevron or the platform-specific back indicator.
- **Recommendation:** Use a platform-aware icon resource or a simple chevron to make the app feel more native on iOS devices.

## The "Polished Code" Suggestion: Enhanced `pokerClickable`
**Currently:** `GameTopBar` manually calls `audioService.playEffect(AudioService.SoundEffect.CLICK)` inside every `onClick` handler. This leads to boilerplate and potential inconsistencies if a developer forgets to add the sound.
**Recommendation:** update `Modifier.pokerClickable` to accept an optional `soundEffect` parameter. By injecting `AudioService` directly within the modifier (via `LocalAppGraph`), we can handle sound playback declaratively.

```kotlin
// Proposed Usage
Modifier.pokerClickable(
    onClick = component::onBack,
    soundEffect = AudioService.SoundEffect.CLICK
)
```
