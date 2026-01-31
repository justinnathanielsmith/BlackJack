---
trigger: glob
globs: ["**/*UI.kt", "**/*Screen.kt", "**/*Content.kt"]
description: Compose Multiplatform Resource Standards
---

# 🎨 UI Resources (Compose 1.10+)

In Compose Multiplatform, always use the generated `Res` class for localized strings, drawables, and other resources.

## 1. Strings
- **Usage**: `stringResource(Res.string.my_key)`
- **Dynamic**: `stringResource(Res.string.welcome_message, userName)`

```kotlin
Text(text = stringResource(Res.string.game_title))
```

## 2. Drawables
- **Usage**: `painterResource(Res.drawable.my_icon)`

```kotlin
Image(
    painter = painterResource(Res.drawable.ic_back),
    contentDescription = stringResource(Res.string.back_button_cd)
)
```

## 3. Fonts
- **Usage**: `Font(Res.font.my_font, weight, style)`

## 4. Rules
- **No Hardcoded Strings**: Every user-facing string MUST be in `strings.xml` and accessed via `Res.string`.
- **Accessibility**: Always provide `contentDescription` for images using `Res.string`.
- **Preview**: Use placeholders or specific test resources for `@Preview` if needed.
