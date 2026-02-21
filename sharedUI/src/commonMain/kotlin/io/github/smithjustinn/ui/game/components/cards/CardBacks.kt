package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.theme.ModernGold
import io.github.smithjustinn.ui.assets.getPreferredColor
import io.github.smithjustinn.ui.assets.toColor
import kotlin.math.abs

// Animation durations (milliseconds)
private const val SHIMMER_ANIMATION_DURATION_MS = 1500
private const val SHIMMER_TRANSLATE_TARGET = 2000f
private const val SHIMMER_OFFSET = 500f

// Rotation angles (degrees)
private const val DIAGONAL_ROTATION = 45f

object CardBacks {
    @Composable
    fun Standard(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { GeometricCardBack(Color(0xFF1A237E)) } // Deep Blue

    @Composable
    fun Dark(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { GeometricCardBack(Color(0xFF263238)) } // Charcoal

    @Composable
    fun Nature(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { GeometricCardBack(Color(0xFF1B5E20)) } // Forest Green

    @Composable
    fun Classic(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { ClassicCardBack(Color(0xFFB71C1C)) } // Deep Red

    @Composable
    fun Pattern(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { PatternCardBack(Color(0xFF4527A0)) } // Deep Purple

    @Composable
    fun Poker(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) { PokerCardBack(Color(0xFF004D40)) } // Deep Teal

    @Composable
    fun Render(
        theme: CardTheme,
        modifier: Modifier = Modifier,
        backgroundColor: Color = theme.backColorHex?.toColor() ?: theme.back.getPreferredColor(),
        rotation: Float = 0f,
    ) {
        val rotationState = rememberUpdatedState(rotation)
        Box(modifier = modifier) {
            CardBack(
                theme = theme.back,
                backColor = backgroundColor,
                rotation = rotationState,
            )
        }
    }

    @Composable
    private fun PreviewContainer(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White), // White border look
        ) {
            Box(
                modifier = Modifier.padding(2.dp).fillMaxSize(),
            ) {
                content()
            }
        }
    }
}

@Composable
@Suppress("ktlint:compose:state-param-check")
internal fun CardBack(
    theme: CardBackTheme,
    backColor: Color,
    rotation: State<Float>,
) {
    val rimLightBrush =
        remember {
            Brush.horizontalGradient(
                colors =
                    listOf(
                        Color.Transparent,
                        Color.White,
                        Color.Transparent,
                    ),
            )
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationY = FULL_ROTATION }
                .drawWithContent {
                    drawContent()

                    val currentRotation = rotation.value
                    val rimLightAlpha = (1f - abs(currentRotation - HALF_ROTATION) / HALF_ROTATION).coerceIn(0f, 1f)

                    if (rimLightAlpha > 0f) {
                        drawRect(
                            brush = rimLightBrush,
                            alpha = rimLightAlpha * HIGH_ALPHA,
                        )
                    }
                },
    ) {
        when (theme) {
            CardBackTheme.GEOMETRIC -> GeometricCardBack(backColor)
            CardBackTheme.CLASSIC -> ClassicCardBack(backColor)
            CardBackTheme.PATTERN -> PatternCardBack(backColor)
            CardBackTheme.POKER -> PokerCardBack(backColor)
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
                    ModernGold.copy(alpha = MEDIUM_ALPHA),
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
internal fun GeometricCardBack(baseColor: Color) {
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val patternColor = Color.White.copy(alpha = LOW_ALPHA)
                    val borderColor = Color.White.copy(alpha = SUBTLE_ALPHA)
                    val borderStroke = Stroke(width = 1.dp.toPx())
                    val rectStroke = Stroke(width = 1.dp.toPx())

                    val step = 16.dp.toPx()
                    val width = size.width
                    val height = size.height
                    val stepInt = step.toInt()
                    val xStart = -stepInt
                    val xEnd = width.toInt() + stepInt
                    val yStart = -stepInt
                    val yEnd = height.toInt() + stepInt

                    val rectSize = Size(step / HALF_DIVISOR, step / HALF_DIVISOR)
                    val borderTopLeft = Offset(8.dp.toPx(), 8.dp.toPx())
                    val borderSize = Size(width - 16.dp.toPx(), height - 16.dp.toPx())
                    val cornerRadius = CornerRadius(8.dp.toPx())

                    onDrawBehind {
                        drawRect(baseColor)

                        for (x in xStart until xEnd step stepInt) {
                            for (y in yStart until yEnd step stepInt) {
                                rotate(DIAGONAL_ROTATION, Offset(x.toFloat(), y.toFloat())) {
                                    drawRect(
                                        color = patternColor,
                                        topLeft = Offset(x.toFloat(), y.toFloat()),
                                        size = rectSize,
                                        style = rectStroke,
                                    )
                                }
                            }
                        }

                        // Inner border
                        drawRoundRect(
                            color = borderColor,
                            topLeft = borderTopLeft,
                            size = borderSize,
                            cornerRadius = cornerRadius,
                            style = borderStroke,
                        )
                    }
                },
    )
}

@Composable
internal fun ClassicCardBack(baseColor: Color) {
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val step = 12.dp.toPx()
                    val color1 = Color.White.copy(alpha = VERY_LOW_ALPHA)
                    val borderColor = Color.White
                    val borderStroke = Stroke(width = 3.dp.toPx())
                    val radius = 2.dp.toPx()

                    val width = size.width
                    val height = size.height
                    val xEnd = (width / step).toInt() + 1
                    val yEnd = (height / step).toInt() + 1

                    val borderTopLeft = Offset(4.dp.toPx(), 4.dp.toPx())
                    val borderSize = Size(width - 8.dp.toPx(), height - 8.dp.toPx())
                    val cornerRadius = CornerRadius(6.dp.toPx())

                    onDrawBehind {
                        drawRect(baseColor)

                        for (x in 0 until xEnd) {
                            for (y in 0 until yEnd) {
                                if ((x + y) % HALF_DIVISOR == 0) {
                                    drawCircle(
                                        color = color1,
                                        radius = radius,
                                        center = Offset(x * step, y * step),
                                    )
                                }
                            }
                        }

                        drawRoundRect(
                            color = borderColor,
                            topLeft = borderTopLeft,
                            size = borderSize,
                            cornerRadius = cornerRadius,
                            style = borderStroke,
                        )
                    }
                },
    )
}

@Composable
internal fun PatternCardBack(baseColor: Color) {
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val path = Path()
                    val step = 20.dp.toPx()
                    val width = size.width
                    val height = size.height

                    for (y in -1 until (height / step).toInt() + 2) {
                        val yPos = y * step
                        path.moveTo(0f, yPos)

                        for (x in 0 until (width / step).toInt() + 1) {
                            val xPos = x * step
                            path.quadraticTo(
                                xPos + step / 2,
                                yPos + if (x % HALF_DIVISOR == 0) step / HALF_DIVISOR else -step / HALF_DIVISOR,
                                xPos + step,
                                yPos,
                            )
                        }
                    }

                    val patternColor = Color.White.copy(alpha = LOW_ALPHA)
                    val strokeWidth = 2.dp.toPx()
                    val borderColor = Color.White.copy(alpha = MEDIUM_ALPHA)
                    val borderStrokeWidth = 1.5.dp.toPx()
                    val borderOffset = 6.dp.toPx()
                    val cornerRadius = 6.dp.toPx()

                    onDrawBehind {
                        drawRect(baseColor)

                        drawPath(
                            path = path,
                            color = patternColor,
                            style = Stroke(width = strokeWidth),
                        )

                        drawRoundRect(
                            color = borderColor,
                            topLeft = Offset(borderOffset, borderOffset),
                            size = Size(size.width - borderOffset * 2, size.height - borderOffset * 2),
                            cornerRadius = CornerRadius(cornerRadius),
                            style = Stroke(width = borderStrokeWidth),
                        )
                    }
                },
    )
}

@Composable
internal fun PokerCardBack(baseColor: Color) {
    Spacer(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val width = size.width
                    val height = size.height

                    val borderSize = 4.dp.toPx()
                    val innerWidth = width - borderSize * 2
                    val innerHeight = height - borderSize * 2
                    val step = 10.dp.toPx()
                    val strokeWidth = 1.dp.toPx()
                    val cornerRadius = CornerRadius(8.dp.toPx())

                    val gridPath = Path()
                    val count = ((innerWidth + innerHeight) / step).toInt()

                    for (i in 0 until count) {
                        val offset = i * step
                        // Diagonal /
                        gridPath.moveTo(borderSize + offset, borderSize)
                        gridPath.lineTo(borderSize, borderSize + offset)

                        // Diagonal \
                        gridPath.moveTo(borderSize, innerHeight + borderSize - offset)
                        gridPath.lineTo(borderSize + offset, innerHeight + borderSize)
                    }

                    val centerX = width / 2
                    val centerY = height / 2
                    val diamondPath =
                        Path().apply {
                            moveTo(centerX, centerY - 20.dp.toPx())
                            lineTo(centerX + 15.dp.toPx(), centerY)
                            lineTo(centerX, centerY + 20.dp.toPx())
                            lineTo(centerX - 15.dp.toPx(), centerY)
                            close()
                        }

                    val patternColor = Color.Black.copy(alpha = 0.15f)
                    val emblemColor1 = Color.White.copy(alpha = 0.2f)
                    val emblemColor2 = Color.White.copy(alpha = 0.5f)
                    val gridStroke = Stroke(width = strokeWidth)
                    val emblemStroke = Stroke(width = 2.dp.toPx())

                    onDrawBehind {
                        // White Border usually
                        drawRect(Color.White)

                        // Inner Color Area
                        drawRoundRect(
                            color = baseColor,
                            topLeft = Offset(borderSize, borderSize),
                            size = Size(innerWidth, innerHeight),
                            cornerRadius = cornerRadius,
                        )

                        // Grid
                        drawPath(
                            path = gridPath,
                            color = patternColor,
                            style = gridStroke,
                        )

                        // Large Center Emblem (Diamond)
                        drawPath(diamondPath, emblemColor1)
                        drawPath(diamondPath, color = emblemColor2, style = emblemStroke)
                    }
                },
    )
}
