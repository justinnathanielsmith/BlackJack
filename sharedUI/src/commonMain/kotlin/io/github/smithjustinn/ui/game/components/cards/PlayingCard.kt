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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.settings_enable_third_eye
import io.github.smithjustinn.resources.card_face_down
import io.github.smithjustinn.resources.card_matched_format
import io.github.smithjustinn.resources.card_name_format
import io.github.smithjustinn.resources.rank_ace
import io.github.smithjustinn.resources.rank_eight
import io.github.smithjustinn.resources.rank_five
import io.github.smithjustinn.resources.rank_four
import io.github.smithjustinn.resources.rank_jack
import io.github.smithjustinn.resources.rank_king
import io.github.smithjustinn.resources.rank_nine
import io.github.smithjustinn.resources.rank_queen
import io.github.smithjustinn.resources.rank_seven
import io.github.smithjustinn.resources.rank_six
import io.github.smithjustinn.resources.rank_ten
import io.github.smithjustinn.resources.rank_three
import io.github.smithjustinn.resources.rank_two
import io.github.smithjustinn.resources.suit_clubs
import io.github.smithjustinn.resources.suit_diamonds
import io.github.smithjustinn.resources.suit_hearts
import io.github.smithjustinn.resources.suit_spades
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.game.components.grid.CARD_ASPECT_RATIO
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

// Animation durations (milliseconds)
private const val SHAKE_ANIMATION_DURATION_MS = 50
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
    wasSeen: Boolean = false,
    isThirdEyeEnabled: Boolean = false,
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

    val contentDescription =
        if (content.visualState.isFaceUp || content.visualState.isMatched) {
            val rankName = stringResource(getRankResource(content.rank))
            val suitName = stringResource(getSuitResource(content.suit))
            val cardName = stringResource(Res.string.card_name_format, rankName, suitName)
            if (content.visualState.isMatched) {
                stringResource(Res.string.card_matched_format, cardName)
            } else {
                cardName
            }
        } else {
            stringResource(Res.string.card_face_down)
        }

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
        contentDescription = contentDescription,
        interactions = CardInteractions(interactionSource = interactionSource, onClick = onClick),
    ) {
        CardContentSelectors(
            content = content,
            rotation = animations.rotation,
            suitColor = suitColor,
            theme = theme,
            backColor = backColor,
            wasSeen = wasSeen,
            isThirdEyeEnabled = isThirdEyeEnabled,
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
    wasSeen: Boolean,
    isThirdEyeEnabled: Boolean,
) {
    val showFace by remember { derivedStateOf { rotation.value <= HALF_ROTATION } }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showFace) {
            CardFace(rank = content.rank, suit = content.suit, suitColor = suitColor, theme = theme.skin)
            if (content.visualState.isRecentlyMatched) ShimmerEffect()
        } else {
            CardBack(
                theme = theme.back,
                backColor = backColor,
                rotation = rotation,
            )
            // Show "Third Eye" indicator if card was seen and feature is enabled
            if (wasSeen && isThirdEyeEnabled && !content.visualState.isMatched) {
                Icon(
                    imageVector = AppIcons.Visibility,
                    contentDescription = stringResource(Res.string.settings_enable_third_eye),
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(PokerTheme.spacing.extraSmall)
                            .size(PokerTheme.spacing.medium),
                    tint = PokerTheme.colors.goldenYellow.copy(alpha = MODERATE_ALPHA),
                )
            }
        }
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
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
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
    contentDescription: String,
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .cardBorder(visuals.rotation, visuals.visualState)
                    .semantics {
                        this.contentDescription = contentDescription
                    },
            shape = PokerTheme.shapes.medium,
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

private fun getRankResource(rank: Rank): StringResource =
    when (rank) {
        Rank.Ace -> Res.string.rank_ace
        Rank.Two -> Res.string.rank_two
        Rank.Three -> Res.string.rank_three
        Rank.Four -> Res.string.rank_four
        Rank.Five -> Res.string.rank_five
        Rank.Six -> Res.string.rank_six
        Rank.Seven -> Res.string.rank_seven
        Rank.Eight -> Res.string.rank_eight
        Rank.Nine -> Res.string.rank_nine
        Rank.Ten -> Res.string.rank_ten
        Rank.Jack -> Res.string.rank_jack
        Rank.Queen -> Res.string.rank_queen
        Rank.King -> Res.string.rank_king
    }

private fun getSuitResource(suit: Suit): StringResource =
    when (suit) {
        Suit.Hearts -> Res.string.suit_hearts
        Suit.Diamonds -> Res.string.suit_diamonds
        Suit.Clubs -> Res.string.suit_clubs
        Suit.Spades -> Res.string.suit_spades
    }
