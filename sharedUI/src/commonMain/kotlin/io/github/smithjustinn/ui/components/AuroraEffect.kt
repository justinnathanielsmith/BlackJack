package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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

    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "phase1",
    )

    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(17000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "phase2",
    )

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        drawAuroraLayers(
            width = size.width,
            height = size.height,
            phase1 = phase1,
            phase2 = phase2,
            baseColor = baseColor,
            accentColor = accentColor,
            highlightColor = highlightColor,
        )
    }
}

private fun DrawScope.drawAuroraLayers(
    width: Float,
    height: Float,
    phase1: Float,
    phase2: Float,
    baseColor: Color,
    accentColor: Color,
    highlightColor: Color,
) {
    // Background glow
    drawRect(
        brush =
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, baseColor.copy(alpha = 0.2f)),
                startY = 0f,
                endY = height,
            ),
    )

    // Layer 1
    drawWave(
        width = width,
        height = height,
        phase = phase1,
        color = baseColor.copy(alpha = 0.2f),
        frequency = 1.0f,
        amplitude = height * 0.1f,
        yOffset = height * 0.5f,
    )

    // Layer 2
    drawWave(
        width = width,
        height = height,
        phase = phase2,
        color = accentColor.copy(alpha = 0.15f),
        frequency = 1.5f,
        amplitude = height * 0.15f,
        yOffset = height * 0.6f,
    )

    // Layer 3
    drawWave(
        width = width,
        height = height,
        phase = phase1 + phase2,
        color = highlightColor.copy(alpha = 0.1f),
        frequency = 2.0f,
        amplitude = height * 0.08f,
        yOffset = height * 0.7f,
    )
}

private fun DrawScope.drawWave(
    width: Float,
    height: Float,
    phase: Float,
    color: Color,
    frequency: Float,
    amplitude: Float,
    yOffset: Float,
) {
    val path = Path()
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

    drawPath(
        path =
        path,
        brush =
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, color),
                startY = yOffset - amplitude,
                endY = height,
            ),
    )
}
