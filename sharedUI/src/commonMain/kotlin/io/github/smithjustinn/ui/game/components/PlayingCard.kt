package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.ClubGreen
import io.github.smithjustinn.theme.DiamondBlue
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.HeartRed
import io.github.smithjustinn.theme.SpadeBlack
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.theme.PokerTheme
import kotlin.math.abs
import kotlin.math.roundToInt

// Animation durations (milliseconds)
private const val SHAKE_ANIMATION_DURATION_MS = 50
private const val FLIP_ANIMATION_DURATION_MS = 400
private const val GLOW_ANIMATION_DURATION_MS = 1000

// Animation values
private const val SHAKE_OFFSET_PX = 10f
private const val CAMERA_DISTANCE_MULTIPLIER = 15f

// Rotation angles (degrees)
internal const val HALF_ROTATION = 90f
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
internal const val HALF_DIVISOR = 2
private const val SHAKE_REPEAT_COUNT = 3

data class CardVisualState(
    val isFaceUp: Boolean,
    val isMatched: Boolean = false,
    val isRecentlyMatched: Boolean = false,
    val isError: Boolean = false,
)

data class CardContent(
    val suit: Suit,
    val rank: Rank,
    val visualState: CardVisualState,
)

data class CardContainerVisuals(
    val visualState: CardVisualState,
    val rotation: Float,
    val scale: Float,
    val matchedGlowAlpha: Float,
)

data class CardInteractions(
    val interactionSource: MutableInteractionSource,
    val onClick: () -> Unit,
)

data class CardAnimations(
    val rotation: Float,
    val scale: Float,
    val shakeOffset: Float,
    val matchedGlowAlpha: Float,
)

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
    val animations = rememberCardAnimations(content, isHovered)
    val suitColor = calculateSuitColor(content.suit, settings.areSuitsMultiColored, settings.symbolTheme)

    CardContainer(
        modifier = modifier.offset { IntOffset(animations.shakeOffset.roundToInt(), 0) },
        visuals =
            CardContainerVisuals(
                visualState = content.visualState,
                rotation = animations.rotation,
                scale = animations.scale,
                matchedGlowAlpha = animations.matchedGlowAlpha,
            ),
        backColor = backColor,
        interactions = CardInteractions(interactionSource = interactionSource, onClick = onClick),
    ) {
        CardContentSelectors(
            content = content,
            rotation = animations.rotation,
            suitColor = suitColor,
            settings = settings,
            backColor = backColor,
        )
    }
}

@Composable
private fun CardContentSelectors(
    content: CardContent,
    rotation: Float,
    suitColor: Color,
    settings: CardDisplaySettings,
    backColor: Color,
) {
    if (rotation <= HALF_ROTATION) {
        CardFace(rank = content.rank, suit = content.suit, suitColor = suitColor, theme = settings.symbolTheme)
        if (content.visualState.isRecentlyMatched) ShimmerEffect()
    } else {
        CardBack(
            theme = settings.backTheme,
            backColor = backColor,
            rotation = rotation,
        )
    }
}

@Composable
private fun rememberCardAnimations(
    content: CardContent,
    isHovered: Boolean,
): CardAnimations {
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

    val matchedGlowAlpha by animateFloatAsState(
        targetValue = if (content.visualState.isRecentlyMatched) SUBTLE_ALPHA else 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(GLOW_ANIMATION_DURATION_MS),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "matchedGlow",
    )

    return CardAnimations(
        rotation = rotation,
        scale = scale,
        shakeOffset = shakeOffset.value,
        matchedGlowAlpha = matchedGlowAlpha,
    )
}

@Composable
private fun CardContainer(
    modifier: Modifier = Modifier,
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
                        } else if (visuals.visualState.isFaceUp && !visuals.visualState.isMatched) {
                            12.dp // Lifted up when viewed
                        } else if (visuals.visualState.isMatched) {
                            2.dp
                        } else {
                            4.dp // Resting state
                        },
                    shape = RoundedCornerShape(12.dp),
                    clip = false,
                    ambientColor = if (visuals.visualState.isRecentlyMatched) GoldenYellow else Color.Black,
                    spotColor = if (visuals.visualState.isRecentlyMatched) GoldenYellow else Color.Black,
                ).drawBehind {
                    if (visuals.visualState.isRecentlyMatched) {
                        drawCircle(
                            color = GoldenYellow.copy(alpha = visuals.matchedGlowAlpha),
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
private fun getCardBorder(
    rotation: Float,
    visualState: CardVisualState,
): BorderStroke =
    if (rotation <= HALF_ROTATION) {
        when {
            visualState.isRecentlyMatched -> BorderStroke(2.dp, GoldenYellow)
            visualState.isMatched -> BorderStroke(1.dp, GoldenYellow.copy(alpha = MEDIUM_ALPHA))
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

private fun calculateSuitColor(
    suit: Suit,
    areSuitsMultiColored: Boolean,
    theme: CardSymbolTheme,
): Color =
    if (theme == CardSymbolTheme.POKER) {
        // Strict Poker Colors
        if (suit.isRed) PokerTheme.CardRed else PokerTheme.CardBlack
    } else if (areSuitsMultiColored) {
        when (suit) {
            Suit.Hearts -> HeartRed
            Suit.Diamonds -> DiamondBlue
            Suit.Clubs -> ClubGreen
            Suit.Spades -> SpadeBlack
        }
    } else {
        if (suit.isRed) HeartRed else SpadeBlack
    }
