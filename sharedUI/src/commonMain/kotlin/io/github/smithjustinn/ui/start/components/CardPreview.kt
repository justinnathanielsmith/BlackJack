package io.github.smithjustinn.ui.start.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.game.components.CardContent
import io.github.smithjustinn.ui.game.components.CardVisualState
import io.github.smithjustinn.ui.game.components.PlayingCard

// Card Preview Layout & Animation Durations
private const val CARD_ROTATION_DURATION = 3000
private const val CARD_FLOAT_DURATION = 2500
private const val CARD_SPACING = -55
private const val CARD_WIDTH = 110
private const val BASE_ROTATION = 12f
private const val CARD_TRANSLATION_Y = 10f
private const val GLOW_SIZE = 220

private const val CARD_MAX_ROTATION_Z = 3f
private const val CARD_MAX_FLOAT_OFFSET = 5f
private const val CARD_FRONT_Z_INDEX = 1f
private const val CARD_BACK_Z_INDEX = 0f

// Background Glow
private const val GLOW_SOFT_BLUE_ALPHA = 0.2f
private const val GLOW_DARK_BLUE_ALPHA = 0.1f

// Star Positions (x, y, size, delay)
private const val STAR_1_OFFSET_X = -70
private const val STAR_1_OFFSET_Y = -60
private const val STAR_1_SIZE = 20
private const val STAR_1_DELAY = 0

private const val STAR_2_OFFSET_X = 80
private const val STAR_2_OFFSET_Y = -50
private const val STAR_2_SIZE = 16
private const val STAR_2_DELAY = 500

private const val STAR_3_OFFSET_X = -80
private const val STAR_3_OFFSET_Y = 40
private const val STAR_3_SIZE = 14
private const val STAR_3_DELAY = 1000

private const val STAR_4_OFFSET_X = 70
private const val STAR_4_OFFSET_Y = 60
private const val STAR_4_SIZE = 18
private const val STAR_4_DELAY = 200

private const val STAR_5_OFFSET_X = 10
private const val STAR_5_OFFSET_Y = -85
private const val STAR_5_SIZE = 10
private const val STAR_5_DELAY = 1500

private const val STAR_6_OFFSET_X = -30
private const val STAR_6_OFFSET_Y = -40
private const val STAR_6_SIZE = 6
private const val STAR_6_DELAY = 800

private const val STAR_7_OFFSET_X = 40
private const val STAR_7_OFFSET_Y = 30
private const val STAR_7_SIZE = 8
private const val STAR_7_DELAY = 1200

/**
 * CardPreview (Visual Section - 2026 Design)
 *
 * Restored the "airy" layout and star animations that provided the feel the user liked,
 * while updating the cards to the Ace of Spades and Ace of Clubs to match the reference image.
 */
@Composable
fun CardPreview(
    modifier: Modifier = Modifier,
    settings: CardDisplaySettings = CardDisplaySettings(),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_preview_anim")

    val rotation by infiniteTransition.animateFloat(
        initialValue = -CARD_MAX_ROTATION_Z,
        targetValue = CARD_MAX_ROTATION_Z,
        animationSpec =
            infiniteRepeatable(
                animation = tween(CARD_ROTATION_DURATION, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "rotation",
    )

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -CARD_MAX_FLOAT_OFFSET,
        targetValue = CARD_MAX_FLOAT_OFFSET,
        animationSpec =
            infiniteRepeatable(
                animation = tween(CARD_FLOAT_DURATION, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "float",
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        BackgroundGlow()
        CardStack(floatOffset, rotation, settings)
        StarsLayer()
    }
}

@Composable
private fun BackgroundGlow() {
    val colors = PokerTheme.colors
    Box(
        modifier =
            Modifier
                .size(GLOW_SIZE.dp)
                .drawBehind {
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        colors.softBlue.copy(alpha = GLOW_SOFT_BLUE_ALPHA),
                                        colors.softBlue.copy(alpha = GLOW_DARK_BLUE_ALPHA),
                                        Color.Transparent,
                                    ),
                            ),
                    )
                },
    )
}

@Composable
private fun CardStack(
    floatOffset: Float,
    rotation: Float,
    settings: CardDisplaySettings,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CARD_SPACING.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.offset(y = floatOffset.dp),
    ) {
        PreviewCard(Suit.Hearts, -BASE_ROTATION + rotation, CARD_FRONT_Z_INDEX, settings)
        PreviewCard(Suit.Spades, BASE_ROTATION - rotation, CARD_BACK_Z_INDEX, settings, CARD_TRANSLATION_Y)
    }
}

@Composable
private fun PreviewCard(
    suit: Suit,
    rotationZ: Float,
    zIndex: Float,
    settings: CardDisplaySettings,
    translationY: Float = 0f,
) {
    PlayingCard(
        content =
            CardContent(
                suit = suit,
                rank = Rank.Ace,
                visualState =
                    CardVisualState(
                        isFaceUp = true,
                        isMatched = false,
                    ),
            ),
        settings = settings,
        modifier =
            Modifier
                .width(CARD_WIDTH.dp)
                .zIndex(zIndex)
                .graphicsLayer {
                    this.rotationZ = rotationZ
                    this.translationY = translationY
                },
    )
}

@Composable
private fun StarsLayer() {
    AnimatedStar(
        Modifier
            .offset(x = STAR_1_OFFSET_X.dp, y = STAR_1_OFFSET_Y.dp)
            .size(STAR_1_SIZE.dp),
        STAR_1_DELAY,
    )
    AnimatedStar(
        Modifier
            .offset(x = STAR_2_OFFSET_X.dp, y = STAR_2_OFFSET_Y.dp)
            .size(STAR_2_SIZE.dp),
        STAR_2_DELAY,
    )
    AnimatedStar(
        Modifier
            .offset(x = STAR_3_OFFSET_X.dp, y = STAR_3_OFFSET_Y.dp)
            .size(STAR_3_SIZE.dp),
        STAR_3_DELAY,
    )
    AnimatedStar(
        Modifier
            .offset(x = STAR_4_OFFSET_X.dp, y = STAR_4_OFFSET_Y.dp)
            .size(STAR_4_SIZE.dp),
        STAR_4_DELAY,
    )
    AnimatedStar(
        Modifier
            .offset(x = STAR_5_OFFSET_X.dp, y = STAR_5_OFFSET_Y.dp)
            .size(STAR_5_SIZE.dp),
        STAR_5_DELAY,
    )
    AnimatedStar(
        Modifier
            .offset(x = STAR_6_OFFSET_X.dp, y = STAR_6_OFFSET_Y.dp)
            .size(STAR_6_SIZE.dp),
        STAR_6_DELAY,
    )
    AnimatedStar(
        Modifier
            .offset(x = STAR_7_OFFSET_X.dp, y = STAR_7_OFFSET_Y.dp)
            .size(STAR_7_SIZE.dp),
        STAR_7_DELAY,
    )
}
