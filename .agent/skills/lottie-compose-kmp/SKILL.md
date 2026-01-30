---
name: lottie-compose-kmp
description: Implements Lottie animations in Kotlin Multiplatform projects using Compose and Compottie. Handles dependency setup, resource placement, and modern implementation patterns while avoiding common deprecations.
---

# Lottie (Compottie) in Compose Multiplatform

This skill guides you through adding Lottie animations to a KMP project using the `compottie` library.

## 1. Dependencies

Ensure `compottie` is added to your version catalog (`libs.versions.toml`) and build files.

### libs.versions.toml
```toml
[versions]
compottie = "2.0.0-rc02" # Check for latest version

[libraries]
compottie = { module = "io.github.alexzhirkevich:compottie", version.ref = "compottie" }
compottie-resources = { module = "io.github.alexzhirkevich:compottie-resources", version.ref = "compottie" }
```

### shared/build.gradle.kts
```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(libs.compottie)
        implementation(libs.compottie.resources)
    }
}
```

## 2. Resources

Place your `.json` animation files in the Compose Resources directory:
`src/commonMain/composeResources/files/your_animation.json`

## 3. Implementation Pattern

Use the following pattern to load and display animations. 
**CRITICAL**: 
- Use `rememberLottieComposition` with `Res.readBytes` inside the lambda for suspendable loading.
- Use `Compottie.IterateForever` instead of `LottieConstants`.
- Use `Image` with `rememberLottiePainter` instead of the deprecated `LottieAnimation` composable.

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
// Import your project's Res and string resources
import io.github.smithjustinn.resources.Res 

@Composable
fun LottieLoader(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/your_animation.json").decodeToString()
        )
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Compottie.IterateForever,
    )

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress }, // Note: progress must be a lambda
        ),
        contentDescription = null,
        modifier = modifier,
    )
}
```

## 4. Common Pitfalls & Fixes

| Issue                    | Solution                                                                                                                        |
| :----------------------- | :------------------------------------------------------------------------------------------------------------------------------ |
| **Crash/Freeze on load** | Ensure you are NOT using `LaunchedEffect` to manually read the bytes into a state var. Use `rememberLottieComposition`'s block. |
| **Deprecation Warnings** | Use `Compottie.IterateForever` instead of `LottieConstants`. Use `rememberLottiePainter` instead of `LottieAnimation`.          |
| **Type Mismatch**        | `rememberLottiePainter`'s `progress` argument expects a lambda `() -> Float`. Pass `{ progress }`.                              |
| **Res Import**           | Ensure you import the correct `Res` object (usually `io.github.yourpackage.resources.Res`).                                     |
