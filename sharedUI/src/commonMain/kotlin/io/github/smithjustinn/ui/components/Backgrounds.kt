package io.github.smithjustinn.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import io.github.smithjustinn.theme.PokerTheme

/**
 * Applies the standard "Premium Poker Felt" background to a Composable.
 * This ensures consistency in colors, darkness, and vignette effects across screens.
 */
@Composable
fun Modifier.pokerBackground(): Modifier {
    val colors = PokerTheme.colors
    return this.background(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        colors.feltGreenCenter,
                        colors.feltGreenTop,
                        colors.feltGreen,
                        colors.feltGreenDark,
                    ),
                center = Offset.Unspecified,
                radius = Float.POSITIVE_INFINITY,
            ),
    )
}
