package io.github.smithjustinn.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * A wrapper that provides a modified [Density] to its children,
 * capping the [Density.fontScale] to prevent UI breakdown at extreme system font sizes.
 */
@Composable
fun AdaptiveDensity(
    maxFontScale: Float = 1.15f,
    content: @Composable () -> Unit,
) {
    val currentDensity = LocalDensity.current
    val cappedFontScale = currentDensity.fontScale.coerceAtMost(maxFontScale)

    val adaptiveDensity =
        Density(
            density = currentDensity.density,
            fontScale = cappedFontScale,
        )

    CompositionLocalProvider(LocalDensity provides adaptiveDensity) {
        content()
    }
}
