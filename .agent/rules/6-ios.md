---
trigger: glob
globs: ["**/iosMain/**/*.kt"]
---

# 🍎 iOS Platform Specifics

## Interop
- Use `@OptIn(ExperimentalForeignApi::class)` only in `iosMain`.
- Update `composeResources` when adding assets.

## Audio
- Use `platform.AVFAudio.AVAudioPlayer` for sound playback.
- Avoid deprecated `AVFoundation` imports.