package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.theme.GoldenYellow
import kotlin.math.abs

// Animation durations (milliseconds)
private const val SHIMMER_ANIMATION_DURATION_MS = 1500
private const val SHIMMER_TRANSLATE_TARGET = 2000f
private const val SHIMMER_OFFSET = 500f

// Rotation angles (degrees)
private const val DIAGONAL_ROTATION = 45f

@Composable
internal fun CardBack(
    theme: CardBackTheme,
    backColor: Color,
    rotation: Float,
) {
    val rimLightAlpha = (1f - (abs(rotation - HALF_ROTATION) / HALF_ROTATION)).coerceIn(0f, 1f)
    val rimLightColor = Color.White.copy(alpha = rimLightAlpha * HIGH_ALPHA)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationY = FULL_ROTATION },
    ) {
        when (theme) {
            CardBackTheme.GEOMETRIC -> GeometricCardBack(backColor)
            CardBackTheme.CLASSIC -> ClassicCardBack(backColor)
            CardBackTheme.PATTERN -> PatternCardBack(backColor)
            CardBackTheme.POKER -> PokerCardBack(backColor)
        }

        // Rim light overlay on the back
        if (rimLightAlpha > 0f) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        rimLightColor,
                                        Color.Transparent,
                                    ),
                            ),
                        ),
            )
        }
    }
}

@Composable
internal fun ShimmerEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_TRANSLATE_TARGET,
        animationSpec =
            infiniteRepeatable(
                animation = tween(SHIMMER_ANIMATION_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
    )

    val brush =
        Brush.linearGradient(
            colors =
                listOf(
                    Color.White.copy(alpha = 0.0f),
                    GoldenYellow.copy(alpha = MEDIUM_ALPHA),
                    Color.White.copy(alpha = 0.0f),
                ),
            start = Offset(translateAnim - SHIMMER_OFFSET, translateAnim - SHIMMER_OFFSET),
            end = Offset(translateAnim, translateAnim),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(brush),
    )
}

@Composable
private fun GeometricCardBack(baseColor: Color) {
    val patternColor = Color.White.copy(alpha = LOW_ALPHA)
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        drawRect(baseColor)

        val step = 16.dp.toPx()
        for (x in -step.toInt() until size.width.toInt() + step.toInt() step step.toInt()) {
            for (y in -step.toInt() until size.height.toInt() + step.toInt() step step.toInt()) {
                rotate(DIAGONAL_ROTATION, Offset(x.toFloat(), y.toFloat())) {
                    drawRect(
                        color = patternColor,
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size =
                            androidx.compose.ui.geometry
                                .Size(step / HALF_DIVISOR, step / HALF_DIVISOR),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
            }
        }

        // Inner border
        drawRoundRect(
            color = Color.White.copy(alpha = SUBTLE_ALPHA),
            topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
            size =
                androidx.compose.ui.geometry
                    .Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
            cornerRadius =
                androidx.compose.ui.geometry
                    .CornerRadius(8.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

@Composable
private fun ClassicCardBack(baseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        drawRect(baseColor)

        // Diamond pattern
        val step = 12.dp.toPx()
        val color1 = Color.White.copy(alpha = VERY_LOW_ALPHA)

        for (x in 0 until (size.width / step).toInt() + 1) {
            for (y in 0 until (size.height / step).toInt() + 1) {
                if ((x + y) % HALF_DIVISOR == 0) {
                    drawCircle(
                        color = color1,
                        radius = 2.dp.toPx(),
                        center = Offset(x * step, y * step),
                    )
                }
            }
        }

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
            size =
                androidx.compose.ui.geometry
                    .Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx()),
            cornerRadius =
                androidx.compose.ui.geometry
                    .CornerRadius(6.dp.toPx()),
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

@Composable
private fun PatternCardBack(baseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        drawRect(baseColor)

        val path = Path()
        val step = 20.dp.toPx()

        for (y in -1 until (size.height / step).toInt() + 2) {
            val yPos = y * step
            path.moveTo(0f, yPos)

            for (x in 0 until (size.width / step).toInt() + 1) {
                val xPos = x * step
                path.quadraticTo(
                    xPos + step / 2,
                    yPos + (if (x % HALF_DIVISOR == 0) step / HALF_DIVISOR else -step / HALF_DIVISOR),
                    xPos + step,
                    yPos,
                )
            }
        }

        drawPath(
            path = path,
            color = Color.White.copy(alpha = LOW_ALPHA),
            style = Stroke(width = 2.dp.toPx()),
        )

        drawRoundRect(
            color = Color.White.copy(alpha = MEDIUM_ALPHA),
            topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
            size =
                androidx.compose.ui.geometry
                    .Size(size.width - 12.dp.toPx(), size.height - 12.dp.toPx()),
            cornerRadius =
                androidx.compose.ui.geometry
                    .CornerRadius(6.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

@Composable
private fun PokerCardBack(baseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        // White Border usually
        drawRect(Color.White)

        // Inner Color Area
        val borderSize = 4.dp.toPx()
        drawRoundRect(
            color = baseColor,
            topLeft = Offset(borderSize, borderSize),
            size =
                androidx.compose.ui.geometry
                    .Size(size.width - borderSize * 2, size.height - borderSize * 2),
            cornerRadius =
                androidx.compose.ui.geometry
                    .CornerRadius(8.dp.toPx()),
        )

        // Diamond Grid Pattern (Classic Casino)
        val step = 10.dp.toPx()
        val patternColor = Color.Black.copy(alpha = 0.15f)

        // Clip to inner area
        val innerWidth = size.width - borderSize * 2
        val innerHeight = size.height - borderSize * 2

        // We can't clip easily in Canvas dsl without native canvas access or clipPath.
        // Instead we allow drawing over and re-draw border or just draw inside carefully.
        // Drawing inside:

        // Draw intersecting lines
        for (i in 0 until ((innerWidth + innerHeight) / step).toInt()) {
            val offset = i * step
            // Diagonal /
            drawLine(
                color = patternColor,
                start = Offset(borderSize + offset, borderSize),
                end = Offset(borderSize, borderSize + offset),
                strokeWidth = 1.dp.toPx(),
            )
            // Diagonal \
            drawLine(
                color = patternColor,
                start = Offset(borderSize, innerHeight + borderSize - offset),
                end = Offset(borderSize + offset, innerHeight + borderSize),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Large Center Emblem (Diamond)
        val centerX = size.width / 2
        val centerY = size.height / 2
        val diamondPath =
            Path().apply {
                moveTo(centerX, centerY - 20.dp.toPx())
                lineTo(centerX + 15.dp.toPx(), centerY)
                lineTo(centerX, centerY + 20.dp.toPx())
                lineTo(centerX - 15.dp.toPx(), centerY)
                close()
            }
        drawPath(diamondPath, Color.White.copy(alpha = 0.2f))
        drawPath(diamondPath, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))
    }
}
