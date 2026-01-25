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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.ClubGreen
import io.github.smithjustinn.theme.DiamondBlue
import io.github.smithjustinn.theme.HeartRed
import io.github.smithjustinn.theme.NeonCyan
import io.github.smithjustinn.theme.SpadeBlack
import io.github.smithjustinn.theme.StartBackgroundTop
import kotlin.math.abs
import kotlin.math.roundToInt

// Animation durations (milliseconds)
private const val SHAKE_ANIMATION_DURATION_MS = 50
private const val FLIP_ANIMATION_DURATION_MS = 400
private const val GLOW_ANIMATION_DURATION_MS = 1000
private const val SHIMMER_ANIMATION_DURATION_MS = 1500
private const val SHIMMER_TRANSLATE_TARGET = 2000f
private const val SHIMMER_OFFSET = 500f

// Animation values
private const val SHAKE_OFFSET_PX = 10f
private const val CAMERA_DISTANCE_MULTIPLIER = 15f

// Rotation angles (degrees)
private const val DIAGONAL_ROTATION = 45f
private const val HALF_ROTATION = 90f
internal const val FULL_ROTATION = 180f

// Alpha values for transparency
private const val RIM_LIGHT_THRESHOLD = 0.1f
internal const val VERY_LOW_ALPHA = 0.15f
internal const val LOW_ALPHA = 0.2f
internal const val SUBTLE_ALPHA = 0.3f
internal const val MEDIUM_ALPHA = 0.4f
internal const val HALF_ALPHA = 0.5f
internal const val MODERATE_ALPHA = 0.6f
internal const val HIGH_ALPHA = 0.8f

// Font size scaling factors
internal const val FONT_SIZE_SMALL = 10f
internal const val FONT_SIZE_MEDIUM = 14f
internal const val FONT_SIZE_LARGE = 16f
internal const val FONT_SIZE_TITLE = 24f
internal const val FONT_SIZE_DISPLAY = 48f
internal const val FONT_SIZE_HERO = 56f
internal const val FONT_SIZE_HUGE = 60f
internal const val BASE_CARD_WIDTH = 80f

// Size ratios and scaling
private const val CARD_ASPECT_RATIO = 0.75f
private const val GLOW_SIZE_MULTIPLIER = 0.75f
private const val BORDER_SIZE_MULTIPLIER = 2f
private const val HALF_DIVISOR = 2
private const val SHAKE_REPEAT_COUNT = 3

data class CardVisualState(
    val isFaceUp: Boolean,
    val isMatched: Boolean = false,
    val isRecentlyMatched: Boolean = false,
    val isError: Boolean = false,
)

data class CardContent(val suit: Suit, val rank: Rank, val visualState: CardVisualState)

data class CardContainerVisuals(
    val visualState: CardVisualState,
    val rotation: Float,
    val scale: Float,
    val matchedGlowAlpha: Float,
)

data class CardInteractions(val interactionSource: MutableInteractionSource, val onClick: () -> Unit)

@Composable
fun PlayingCard(
    content: CardContent,
    modifier: Modifier = Modifier,
    backColor: Color = StartBackgroundTop,
    settings: CardDisplaySettings = CardDisplaySettings(),
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val rotation by animateFloatAsState(
        targetValue = if (content.visualState.isFaceUp) 0f else FULL_ROTATION,
        animationSpec = tween(durationMillis = FLIP_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

    val scale by animateFloatAsState(
        targetValue =
        if (content.visualState.isRecentlyMatched || (isHovered && !content.visualState.isFaceUp)) {
            1.05f
        } else {
            1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pulse",
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(content.visualState.isError) {
        if (content.visualState.isError) {
            repeat(SHAKE_REPEAT_COUNT) {
                shakeOffset.animateTo(SHAKE_OFFSET_PX, tween(SHAKE_ANIMATION_DURATION_MS))
                shakeOffset.animateTo(-SHAKE_OFFSET_PX, tween(SHAKE_ANIMATION_DURATION_MS))
            }
            shakeOffset.animateTo(0f, tween(SHAKE_ANIMATION_DURATION_MS))
        }
    }

    val suitColor = calculateSuitColor(content.suit, settings.areSuitsMultiColored)
    val matchedGlowAlpha by animateFloatAsState(
        targetValue = if (content.visualState.isRecentlyMatched) SUBTLE_ALPHA else 0f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(GLOW_ANIMATION_DURATION_MS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "matchedGlow",
    )

    CardContainer(
        modifier = modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        visuals = CardContainerVisuals(
            visualState = content.visualState,
            rotation = rotation,
            scale = scale,
            matchedGlowAlpha = matchedGlowAlpha,
        ),
        backColor = backColor,
        interactions = CardInteractions(interactionSource = interactionSource, onClick = onClick),
    ) {
        if (rotation <= HALF_ROTATION) {
            CardFace(rank = content.rank, suit = content.suit, suitColor = suitColor, theme = settings.symbolTheme)
            if (content.visualState.isRecentlyMatched) ShimmerEffect()
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
    visuals: CardContainerVisuals,
    backColor: Color,
    interactions: CardInteractions,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
        modifier
            .widthIn(min = 60.dp)
            .aspectRatio(CARD_ASPECT_RATIO)
            .graphicsLayer {
                rotationY = visuals.rotation
                scaleX = visuals.scale
                scaleY = visuals.scale
                cameraDistance = CAMERA_DISTANCE_MULTIPLIER * density
            }.shadow(
                elevation =
                if (visuals.visualState.isRecentlyMatched) {
                    10.dp
                } else if (visuals.visualState.isMatched) {
                    2.dp
                } else {
                    6.dp
                },
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor = if (visuals.visualState.isRecentlyMatched) NeonCyan else Color.Black,
                spotColor = if (visuals.visualState.isRecentlyMatched) NeonCyan else Color.Black,
            ).drawBehind {
                if (visuals.visualState.isRecentlyMatched) {
                    drawCircle(
                        color = NeonCyan.copy(alpha = visuals.matchedGlowAlpha),
                        radius = size.maxDimension * GLOW_SIZE_MULTIPLIER,
                        center = center,
                    )
                }
            },
    ) {
        Card(
            onClick = interactions.onClick,
            interactionSource = interactions.interactionSource,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = if (visuals.rotation <= HALF_ROTATION) Color.White else backColor,
            ),
            border = getCardBorder(visuals.rotation, visuals.visualState),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
private fun getCardBorder(rotation: Float, visualState: CardVisualState): BorderStroke =
    if (rotation <= HALF_ROTATION) {
        when {
            visualState.isRecentlyMatched -> BorderStroke(2.dp, NeonCyan)
            visualState.isMatched -> BorderStroke(1.dp, NeonCyan.copy(alpha = MEDIUM_ALPHA))
            visualState.isError -> BorderStroke(3.dp, MaterialTheme.colorScheme.error)
            else -> BorderStroke(1.dp, Color.LightGray.copy(alpha = HALF_ALPHA))
        }
    } else {
        val rimLightAlpha = (1f - (abs(rotation - HALF_ROTATION) / HALF_ROTATION)).coerceIn(0f, 1f)
        val rimLightColor = Color.White.copy(alpha = rimLightAlpha * HIGH_ALPHA)
        BorderStroke(
            width = (2.dp + (rimLightAlpha * BORDER_SIZE_MULTIPLIER).dp),
            color = if (rimLightAlpha > RIM_LIGHT_THRESHOLD) rimLightColor else Color.White.copy(alpha = SUBTLE_ALPHA),
        )
    }

private fun calculateSuitColor(suit: Suit, areSuitsMultiColored: Boolean): Color = if (areSuitsMultiColored) {
    when (suit) {
        Suit.Hearts -> HeartRed
        Suit.Diamonds -> DiamondBlue
        Suit.Clubs -> ClubGreen
        Suit.Spades -> SpadeBlack
    }
} else {
    if (suit.isRed) HeartRed else SpadeBlack
}

@Composable
private fun CardBack(theme: CardBackTheme, backColor: Color, rotation: Float) {
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
fun ShimmerEffect() {
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
                NeonCyan.copy(alpha = MEDIUM_ALPHA),
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
