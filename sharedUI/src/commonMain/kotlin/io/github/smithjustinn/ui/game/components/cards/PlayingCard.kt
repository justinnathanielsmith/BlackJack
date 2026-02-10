package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.game.components.grid.CARD_ASPECT_RATIO
import kotlin.math.roundToInt

// Animation durations (milliseconds)
private const val SHAKE_ANIMATION_DURATION_MS = 50
private const val FLIP_ANIMATION_DURATION_MS = 400
private const val GLOW_ANIMATION_DURATION_MS = 1000

// Animation values
private const val SHAKE_OFFSET_PX = 10f
private const val CAMERA_DISTANCE_MULTIPLIER = 15f
private const val MUCK_TARGET_Y_FALLBACK = 1500

// Rotation angles (degrees)
internal const val HALF_ROTATION = 90f
internal const val FULL_ROTATION = 180f

// Alpha values for transparency

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
// CARD_ASPECT_RATIO is now shared from GameGridMetrics.kt
private const val GLOW_SIZE_MULTIPLIER = 0.75f
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
    val rotation: State<Float>,
    val scale: State<Float>,
    val matchedGlowAlpha: State<Float>,
    val shadowElevation: State<Dp>,
    val shadowYOffset: State<Dp>,
)

data class CardInteractions(
    val interactionSource: MutableInteractionSource,
    val onClick: () -> Unit,
)

data class CardAnimations(
    val rotation: State<Float>,
    val scale: State<Float>,
    val shakeOffset: State<Float>,
    val matchedGlowAlpha: State<Float>,
    val muckTranslationX: State<Float>,
    val muckTranslationY: State<Float>,
    val muckRotation: State<Float>,
    val muckScale: State<Float>,
    val shadowElevation: State<Dp>,
    val shadowYOffset: State<Dp>,
)

@Composable
fun PlayingCard(
    content: CardContent,
    modifier: Modifier = Modifier,
    backColor: Color = PokerTheme.colors.feltGreen,
    theme: CardTheme = CardTheme(),
    areSuitsMultiColored: Boolean = false,
    muckTargetOffset: IntOffset = IntOffset(0, MUCK_TARGET_Y_FALLBACK), // Default to flying off bottom
    muckTargetRotation: Float = 15f,
    isMuckingEnabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animations =
        rememberCardAnimations(
            content = content,
            isHovered = isHovered,
            muckTargetOffset = muckTargetOffset,
            muckTargetRotation = muckTargetRotation,
            isMuckingEnabled = isMuckingEnabled,
        )
    val suitColor = calculateSuitColor(content.suit, areSuitsMultiColored, theme.skin)

    CardContainer(
        modifier = modifier.offset { IntOffset(animations.shakeOffset.value.roundToInt(), 0) },
        visuals =
            CardContainerVisuals(
                visualState = content.visualState,
                rotation = animations.rotation,
                scale = if (content.visualState.isMatched) animations.muckScale else animations.scale,
                matchedGlowAlpha = animations.matchedGlowAlpha,
                shadowElevation = animations.shadowElevation,
                shadowYOffset = animations.shadowYOffset,
            ),
        muckTranslationX = animations.muckTranslationX,
        muckTranslationY = animations.muckTranslationY,
        muckRotation = animations.muckRotation,
        backColor = backColor,
        interactions = CardInteractions(interactionSource = interactionSource, onClick = onClick),
    ) {
        CardContentSelectors(
            content = content,
            rotation = animations.rotation,
            suitColor = suitColor,
            theme = theme,
            backColor = backColor,
        )
    }
}

@Composable
@Suppress("ktlint:compose:state-param-check")
private fun CardContentSelectors(
    content: CardContent,
    rotation: State<Float>,
    suitColor: Color,
    theme: CardTheme,
    backColor: Color,
) {
    val showFace by remember { derivedStateOf { rotation.value <= HALF_ROTATION } }

    if (showFace) {
        CardFace(rank = content.rank, suit = content.suit, suitColor = suitColor, theme = theme.skin)
        if (content.visualState.isRecentlyMatched) ShimmerEffect()
    } else {
        CardBack(
            theme = theme.back,
            backColor = backColor,
            rotation = rotation.value,
        )
    }
}

private const val MATCHED_SCALE = 0.4f
private const val PULSE_SCALE = 1.05f
private const val DEFAULT_SCALE = 1f
private const val MUCK_DURATION_MS = 600
private const val ELEVATION_RECENTLY_MATCHED = 12
private const val ELEVATION_MATCHED = 0
private const val ELEVATION_HOVERED = 16
private const val ELEVATION_DEFAULT = 2
private const val SHADOW_OFFSET_DIVISOR = 3

@Composable
private fun rememberCardAnimations(
    content: CardContent,
    isHovered: Boolean,
    muckTargetOffset: IntOffset,
    muckTargetRotation: Float,
    isMuckingEnabled: Boolean,
): CardAnimations {
    val rotation = rememberFlipAnimation(content.visualState.isFaceUp)
    val scale = rememberPulseAnimation(content.visualState, isHovered)
    val shakeOffset = rememberShakeAnimation(content.visualState.isError)
    val matchedGlowAlpha = rememberMatchedGlowAnimation(content.visualState.isRecentlyMatched)

    val muckTranslationX =
        rememberMuckAnimation(
            isMuckingEnabled && content.visualState.isMatched,
            muckTargetOffset.x.toFloat(),
            "muckTranslationX",
        )
    val muckTranslationY =
        rememberMuckAnimation(
            isMuckingEnabled && content.visualState.isMatched,
            muckTargetOffset.y.toFloat(),
            "muckTranslationY",
        )
    val muckRotation =
        rememberMuckAnimation(
            isMuckingEnabled && content.visualState.isMatched,
            muckTargetRotation,
            "muckRotation",
        )

    val shadowAnim = rememberShadowAnimation(content.visualState, isHovered)

    return CardAnimations(
        rotation = rotation,
        scale = scale,
        shakeOffset = shakeOffset,
        matchedGlowAlpha = matchedGlowAlpha,
        muckTranslationX = muckTranslationX,
        muckTranslationY = muckTranslationY,
        muckRotation = muckRotation,
        muckScale = scale,
        shadowElevation = shadowAnim.first,
        shadowYOffset = shadowAnim.second,
    )
}

@Composable
private fun rememberFlipAnimation(isFaceUp: Boolean) =
    animateFloatAsState(
        targetValue = if (isFaceUp) 0f else FULL_ROTATION,
        animationSpec = tween(durationMillis = FLIP_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

@Composable
private fun rememberPulseAnimation(
    visualState: CardVisualState,
    isHovered: Boolean,
) = animateFloatAsState(
    targetValue =
        when {
            visualState.isMatched -> MATCHED_SCALE
            visualState.isRecentlyMatched || (isHovered && !visualState.isFaceUp) -> PULSE_SCALE
            else -> DEFAULT_SCALE
        },
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "pulse",
)

@Composable
private fun rememberShakeAnimation(isError: Boolean): State<Float> {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            repeat(SHAKE_REPEAT_COUNT) {
                shakeOffset.animateTo(SHAKE_OFFSET_PX, tween(SHAKE_ANIMATION_DURATION_MS))
                shakeOffset.animateTo(-SHAKE_OFFSET_PX, tween(SHAKE_ANIMATION_DURATION_MS))
            }
            shakeOffset.animateTo(0f, tween(SHAKE_ANIMATION_DURATION_MS))
        }
    }
    return shakeOffset.asState()
}

@Composable
private fun rememberMatchedGlowAnimation(isRecentlyMatched: Boolean) =
    animateFloatAsState(
        targetValue = if (isRecentlyMatched) SUBTLE_ALPHA else 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(GLOW_ANIMATION_DURATION_MS),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "matchedGlow",
    )

@Composable
private fun rememberMuckAnimation(
    isActive: Boolean,
    target: Float,
    label: String,
) = animateFloatAsState(
    targetValue = if (isActive) target else 0f,
    animationSpec = tween(durationMillis = MUCK_DURATION_MS, easing = FastOutSlowInEasing),
    label = label,
)

@Composable
private fun rememberShadowAnimation(
    visualState: CardVisualState,
    isHovered: Boolean,
): Pair<State<Dp>, State<Dp>> {
    val targetElevation =
        when {
            visualState.isRecentlyMatched -> ELEVATION_RECENTLY_MATCHED.dp
            visualState.isMatched -> ELEVATION_MATCHED.dp
            visualState.isFaceUp || isHovered -> ELEVATION_HOVERED.dp
            else -> ELEVATION_DEFAULT.dp
        }

    val shadowElevation =
        animateDpAsState(
            targetValue = targetElevation,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "shadowElevation",
        )

    val shadowYOffset =
        animateDpAsState(
            targetValue = (targetElevation.value / SHADOW_OFFSET_DIVISOR).dp,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "shadowYOffset",
        )

    return shadowElevation to shadowYOffset
}

@Composable
@Suppress("ktlint:compose:state-param-check")
private fun CardContainer(
    modifier: Modifier = Modifier,
    visuals: CardContainerVisuals,
    muckTranslationX: State<Float>,
    muckTranslationY: State<Float>,
    muckRotation: State<Float>,
    backColor: Color,
    interactions: CardInteractions,
    content: @Composable () -> Unit,
) {
    val glowColor = PokerTheme.colors.goldenYellow
    val isFaceUpVisual by remember { derivedStateOf { visuals.rotation.value <= HALF_ROTATION } }

    Box(
        modifier =
            modifier
                .widthIn(min = 60.dp)
                .aspectRatio(CARD_ASPECT_RATIO)
                .graphicsLayer {
                    translationX = muckTranslationX.value
                    translationY = muckTranslationY.value
                    rotationZ = muckRotation.value
                    rotationY = visuals.rotation.value
                    scaleX = visuals.scale.value
                    scaleY = visuals.scale.value
                    cameraDistance = CAMERA_DISTANCE_MULTIPLIER * density
                    alpha = 1f
                }.drawBehind {
                    if (visuals.visualState.isRecentlyMatched) {
                        drawCircle(
                            color = glowColor.copy(alpha = visuals.matchedGlowAlpha.value),
                            radius = size.maxDimension * GLOW_SIZE_MULTIPLIER,
                            center = center,
                        )
                    }
                },
    ) {
        CardShadowLayer(
            elevation = visuals.shadowElevation,
            yOffset = visuals.shadowYOffset,
            isRecentlyMatched = visuals.visualState.isRecentlyMatched,
        )

        Card(
            onClick = interactions.onClick,
            interactionSource = interactions.interactionSource,
            modifier = Modifier.fillMaxSize().cardBorder(visuals.rotation, visuals.visualState),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = if (isFaceUpVisual) Color.White else backColor,
                ),
            border = null,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
