package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import io.github.smithjustinn.theme.Brass
import io.github.smithjustinn.theme.Bronze
import io.github.smithjustinn.theme.ModernGold

// Increased duration for a slower, more subtle effect
private const val SHIMMER_DURATION_MS = 4000
private const val SHIMMER_MAX_OFFSET = 2000f
private const val SHIMMER_START_OFFSET = 1000f

@Composable
fun rememberGlimmerBrush(): Brush {
    val shimmerColors =
        listOf(
            Bronze,
            ModernGold,
            Bronze,
            Brass,
            Bronze,
            ModernGold,
            Bronze,
            Brass,
            Bronze,
        )

    val transition = rememberInfiniteTransition(label = "glimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_MAX_OFFSET,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SHIMMER_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "glimmer_translate",
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - SHIMMER_START_OFFSET, translateAnim - SHIMMER_START_OFFSET),
        end = Offset(translateAnim, translateAnim),
        tileMode = TileMode.Clamp,
    )
}
