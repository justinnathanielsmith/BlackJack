package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.EmeraldGreen
import io.github.smithjustinn.theme.ModernGold
import io.github.smithjustinn.theme.SoftBlue
import kotlin.math.PI
import kotlin.math.sin

/**
 * An animated aurora/glow effect designed to be placed at the bottom of screens.
 * Creates a premium, dream-like atmosphere with slowly moving wave gradients.
 *
 * Performance optimized:
 * - Uses [Modifier.drawWithCache] to cache expensive objects (Path, Brush)
 * - Reads animation state only in the draw phase to skip Composition/Layout
 * - Reuses a single [Path] object to prevent per-frame allocation
 */
@Composable
fun AuroraEffect(
    modifier: Modifier = Modifier,
    height: Dp = 250.dp,
    baseColor: Color = EmeraldGreen,
    accentColor: Color = ModernGold,
    highlightColor: Color = SoftBlue,
) {
    val transition = rememberInfiniteTransition(label = "aurora")

    // Use State objects directly to defer reads to the draw phase
    val phase1State = transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec =
        infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase1",
    )

    val phase2State = transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec =
        infiniteRepeatable(
            animation = tween(17000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase2",
    )

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawWithCache {
                val width = size.width
                val heightPx = size.height

                // Cache brushes as they depend only on size/colors, not animation phase
                val bgBrush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, baseColor.copy(alpha = 0.2f)),
                    startY = 0f,
                    endY = heightPx,
                )

                // Layer 1 config
                val l1Amp = heightPx * 0.1f
                val l1Y = heightPx * 0.5f
                val l1Brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, baseColor.copy(alpha = 0.2f)),
                    startY = l1Y - l1Amp,
                    endY = heightPx,
                )

                // Layer 2 config
                val l2Amp = heightPx * 0.15f
                val l2Y = heightPx * 0.6f
                val l2Brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.15f)),
                    startY = l2Y - l2Amp,
                    endY = heightPx,
                )

                // Layer 3 config
                val l3Amp = heightPx * 0.08f
                val l3Y = heightPx * 0.7f
                val l3Brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, highlightColor.copy(alpha = 0.1f)),
                    startY = l3Y - l3Amp,
                    endY = heightPx,
                )

                // Reuse a single Path object for all waves to avoid allocation
                val path = Path()

                onDrawBehind {
                    // Read state inside draw scope to trigger only redraws
                    val p1 = phase1State.value
                    val p2 = phase2State.value

                    drawRect(brush = bgBrush)

                    // Layer 1
                    path.reset()
                    buildWavePath(path, width, heightPx, p1, 1.0f, l1Amp, l1Y)
                    drawPath(path, l1Brush)

                    // Layer 2
                    path.reset()
                    buildWavePath(path, width, heightPx, p2, 1.5f, l2Amp, l2Y)
                    drawPath(path, l2Brush)

                    // Layer 3
                    path.reset()
                    buildWavePath(path, width, heightPx, p1 + p2, 2.0f, l3Amp, l3Y)
                    drawPath(path, l3Brush)
                }
            },
    )
}

private fun buildWavePath(
    path: Path,
    width: Float,
    height: Float,
    phase: Float,
    frequency: Float,
    amplitude: Float,
    yOffset: Float,
) {
    val steps = 50
    val stepSize = width / steps

    path.moveTo(0f, height)
    path.lineTo(0f, yOffset)

    for (i in 0..steps) {
        val x = i * stepSize
        // Combined sine waves for organic look
        val y =
            yOffset +
                sin(x / width * 2 * PI * frequency + phase) * amplitude +
                sin(x / width * PI * frequency * 0.5 - phase) * (amplitude * 0.5f)
        path.lineTo(x, y.toFloat())
    }

    path.lineTo(width, height)
    path.close()
}
