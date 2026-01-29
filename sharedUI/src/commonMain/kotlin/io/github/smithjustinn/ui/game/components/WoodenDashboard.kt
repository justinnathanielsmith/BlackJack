package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.AppColors
import io.github.smithjustinn.theme.PokerTheme

private const val GLOW_ALPHA_INITIAL = 0.5f
private const val GLOW_ALPHA_TARGET = 1.0f
private const val GLOW_DURATION_MS = 800
private const val GLOW_HEIGHT_FACTOR = 0.8f
private const val GLOW_RIM_ALPHA_FACTOR = 0.8f
private const val BEVEL_ALPHA = 0.15f
private const val SHADOW_ALPHA = 0.3f
private const val GRAIN_ALPHA = 0.05f
private const val GRAIN_POS_1 = 0.3f
private const val GRAIN_POS_2 = 0.7f
private const val GLOW_SECONDARY_ALPHA = 0.2f
private const val GLOW_RIM_STROKE_WIDTH = 4
private const val BEVEL_STROKE_WIDTH = 2
private const val SHADOW_STROKE_WIDTH = 4
private const val GRAIN_STROKE_WIDTH = 1

@Composable
fun WoodenDashboard(
    modifier: Modifier = Modifier,
    isHeatMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    val infiniteTransition = rememberInfiniteTransition(label = "woodenDashboard")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = GLOW_ALPHA_INITIAL,
        targetValue = GLOW_ALPHA_TARGET,
        animationSpec =
            infiniteRepeatable(
                animation = tween(GLOW_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glowAlpha",
    )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.oakWood)
                .drawBehind {
                    if (isHeatMode) {
                        drawHeatModeGlow(colors, glowAlpha)
                    }

                    drawBevels()
                    drawGrainAccents()
                }.padding(vertical = spacing.small),
    ) {
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeatModeGlow(
    colors: io.github.smithjustinn.theme.AppColors,
    glowAlpha: Float,
) {
    drawRect(
        brush =
            Brush.verticalGradient(
                colors =
                    listOf(
                        colors.tacticalRed.copy(alpha = glowAlpha),
                        colors.tacticalRed.copy(alpha = GLOW_SECONDARY_ALPHA),
                        Color.Transparent,
                    ),
                startY = 0f,
                endY = size.height * GLOW_HEIGHT_FACTOR,
            ),
        blendMode = BlendMode.Screen,
    )

    drawLine(
        brush =
            Brush.horizontalGradient(
                colors =
                    listOf(
                        Color.Transparent,
                        colors.goldenYellow.copy(alpha = glowAlpha * GLOW_RIM_ALPHA_FACTOR),
                        Color.Transparent,
                    ),
            ),
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = GLOW_RIM_STROKE_WIDTH.dp.toPx(),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBevels() {
    // Outer Bevel (Bottom Highlight)
    drawLine(
        color = Color.White.copy(alpha = BEVEL_ALPHA),
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = BEVEL_STROKE_WIDTH.dp.toPx(),
    )

    // Inner Shadow (Top)
    drawLine(
        color = Color.Black.copy(alpha = SHADOW_ALPHA),
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = SHADOW_STROKE_WIDTH.dp.toPx(),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrainAccents() {
    val grainColor = Color.Black.copy(alpha = GRAIN_ALPHA)
    val grainWidth = GRAIN_STROKE_WIDTH.dp.toPx()
    drawLine(
        grainColor,
        Offset(0f, size.height * GRAIN_POS_1),
        Offset(size.width, size.height * GRAIN_POS_1),
        grainWidth,
    )
    drawLine(
        grainColor,
        Offset(0f, size.height * GRAIN_POS_2),
        Offset(size.width, size.height * GRAIN_POS_2),
        grainWidth,
    )
}
