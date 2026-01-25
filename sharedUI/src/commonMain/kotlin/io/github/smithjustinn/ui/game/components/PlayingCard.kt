package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun PlayingCard(
    suit: Suit,
    rank: Rank,
    isFaceUp: Boolean,
    isMatched: Boolean = false,
    isRecentlyMatched: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    backColor: Color = StartBackgroundTop,
    settings: CardDisplaySettings = CardDisplaySettings(),
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

    val scale by animateFloatAsState(
        targetValue = if (isRecentlyMatched || (isHovered && !isFaceUp)) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pulse",
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            repeat(3) {
                shakeOffset.animateTo(10f, tween(50))
                shakeOffset.animateTo(-10f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    val suitColor = calculateSuitColor(suit, settings.areSuitsMultiColored)
    val matchedGlowAlpha by animateFloatAsState(
        targetValue = if (isRecentlyMatched) 0.3f else 0f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
    )

    CardContainer(
        modifier = modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        rotation = rotation,
        scale = scale,
        isMatched = isMatched,
        isRecentlyMatched = isRecentlyMatched,
        isError = isError,
        matchedGlowAlpha = matchedGlowAlpha,
        backColor = backColor,
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        if (rotation <= 90f) {
            CardFace(rank = rank, suit = suit, suitColor = suitColor, theme = settings.symbolTheme)
            if (isRecentlyMatched) ShimmerEffect()
        } else {
            CardBack(
                theme = settings.backTheme,
                backColor = backColor,
                rotation = rotation, // Pass rotation to CardBack for rim light calculation
            )
        }
    }
}

@Composable
private fun CardContainer(
    modifier: Modifier,
    rotation: Float,
    scale: Float,
    isMatched: Boolean,
    isRecentlyMatched: Boolean,
    isError: Boolean,
    matchedGlowAlpha: Float,
    backColor: Color,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .widthIn(min = 60.dp)
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY = rotation
                scaleX = scale
                scaleY = scale
                cameraDistance = 15f * density
            }
            .shadow(
                elevation = if (isRecentlyMatched) 10.dp else if (isMatched) 2.dp else 6.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor = if (isRecentlyMatched) NeonCyan else Color.Black,
                spotColor = if (isRecentlyMatched) NeonCyan else Color.Black,
            )
            .drawBehind {
                if (isRecentlyMatched) {
                    drawCircle(
                        color = NeonCyan.copy(alpha = matchedGlowAlpha),
                        radius = size.maxDimension * 0.75f,
                        center = center,
                    )
                }
            },
    ) {
        Card(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (rotation <= 90f) Color.White else backColor,
            ),
            border = getCardBorder(rotation, isRecentlyMatched, isMatched, isError),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
private fun getCardBorder(
    rotation: Float,
    isRecentlyMatched: Boolean,
    isMatched: Boolean,
    isError: Boolean,
): BorderStroke {
    return if (rotation <= 90f) {
        when {
            isRecentlyMatched -> BorderStroke(2.dp, NeonCyan)
            isMatched -> BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
            isError -> BorderStroke(3.dp, MaterialTheme.colorScheme.error)
            else -> BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        }
    } else {
        val rimLightAlpha = (1f - (abs(rotation - 90f) / 90f)).coerceIn(0f, 1f)
        val rimLightColor = Color.White.copy(alpha = rimLightAlpha * 0.8f)
        BorderStroke(
            width = (2.dp + (rimLightAlpha * 2).dp),
            color = if (rimLightAlpha > 0.1f) rimLightColor else Color.White.copy(alpha = 0.3f),
        )
    }
}

private fun calculateSuitColor(suit: Suit, areSuitsMultiColored: Boolean): Color {
    return if (areSuitsMultiColored) {
        when (suit) {
            Suit.Hearts -> HeartRed
            Suit.Diamonds -> DiamondBlue
            Suit.Clubs -> ClubGreen
            Suit.Spades -> SpadeBlack
        }
    } else {
        if (suit.isRed) HeartRed else SpadeBlack
    }
}

@Composable
private fun CardBack(
    theme: CardBackTheme,
    backColor: Color,
    rotation: Float,
) {
    val rimLightAlpha = (1f - (abs(rotation - 90f) / 90f)).coerceIn(0f, 1f)
    val rimLightColor = Color.White.copy(alpha = rimLightAlpha * 0.8f)

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f },
    ) {
        when (theme) {
            CardBackTheme.GEOMETRIC -> GeometricCardBack(backColor)
            CardBackTheme.CLASSIC -> ClassicCardBack(backColor)
            CardBackTheme.PATTERN -> PatternCardBack(backColor)
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
private fun CardFace(rank: Rank, suit: Suit, suitColor: Color, theme: CardSymbolTheme) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        // Base scaling on the card width (maxWidth is a Dp value)
        val baseSize = maxWidth
        val fontScale = density.fontScale

        // Helper to get size that ignores system font scaling
        fun getFontSize(size: Float) = (size * (baseSize.value / 80f) / fontScale).sp

        when (theme) {
            CardSymbolTheme.CLASSIC -> {
                // Top Left
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = rank.symbol,
                        color = suitColor,
                        style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = getFontSize(14f),
                        ),
                    )
                    Text(
                        text = suit.symbol,
                        color = suitColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = getFontSize(10f)),
                    )
                }

                // Center Suit
                Text(
                    text = suit.symbol,
                    color = suitColor.copy(alpha = 0.15f),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = getFontSize(60f)),
                    modifier = Modifier.align(Alignment.Center),
                )

                Text(
                    text = rank.symbol,
                    color = suitColor,
                    style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = getFontSize(24f),
                    ),
                    modifier = Modifier.align(Alignment.Center),
                )

                // Bottom Right
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { rotationZ = 180f },
                ) {
                    Text(
                        text = rank.symbol,
                        color = suitColor,
                        style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = getFontSize(14f),
                        ),
                    )
                    Text(
                        text = suit.symbol,
                        color = suitColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = getFontSize(10f)),
                    )
                }
            }

            CardSymbolTheme.MINIMAL -> {
                // Large Rank in Center
                Text(
                    text = rank.symbol,
                    color = suitColor,
                    style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = getFontSize(48f),
                    ),
                    modifier = Modifier.align(Alignment.Center),
                )

                // Small Suit in Corners
                Text(
                    text = suit.symbol,
                    color = suitColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = getFontSize(16f)),
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                )

                Text(
                    text = suit.symbol,
                    color = suitColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = getFontSize(16f)),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).graphicsLayer { rotationZ = 180f },
                )
            }

            CardSymbolTheme.TEXT_ONLY -> {
                // Just the rank, no suit symbols
                Text(
                    text = rank.symbol,
                    color = suitColor,
                    style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = getFontSize(56f),
                    ),
                    modifier = Modifier.align(Alignment.Center),
                )

                // Suit name instead of symbol at bottom
                Text(
                    text = suit.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = suitColor.copy(alpha = 0.5f),
                    style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = getFontSize(10f),
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
fun ShimmerEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val brush =
        Brush.linearGradient(
            colors =
            listOf(
                Color.White.copy(alpha = 0.0f),
                NeonCyan.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.0f),
            ),
            start = Offset(translateAnim - 500f, translateAnim - 500f),
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
    val patternColor = Color.White.copy(alpha = 0.2f)
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        drawRect(baseColor)

        val step = 16.dp.toPx()
        for (x in -step.toInt() until size.width.toInt() + step.toInt() step step.toInt()) {
            for (y in -step.toInt() until size.height.toInt() + step.toInt() step step.toInt()) {
                rotate(45f, Offset(x.toFloat(), y.toFloat())) {
                    drawRect(
                        color = patternColor,
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size =
                        androidx.compose.ui.geometry
                            .Size(step / 2, step / 2),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
            }
        }

        // Inner border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
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
        val color1 = Color.White.copy(alpha = 0.15f)

        for (x in 0 until (size.width / step).toInt() + 1) {
            for (y in 0 until (size.height / step).toInt() + 1) {
                if ((x + y) % 2 == 0) {
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
                    yPos + (if (x % 2 == 0) step / 2 else -step / 2),
                    xPos + step,
                    yPos,
                )
            }
        }

        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.2f),
            style = Stroke(width = 2.dp.toPx()),
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
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
