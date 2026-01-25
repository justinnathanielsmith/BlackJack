package io.github.smithjustinn.ui.start.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.DarkBlue
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.SoftBlue
import io.github.smithjustinn.ui.game.components.PlayingCard

private const val CARD_ROTATION_DURATION = 3000
private const val CARD_FLOAT_DURATION = 2500
private const val CARD_SPACING = -55
private const val CARD_WIDTH = 110
private const val BASE_ROTATION = 12f
private const val CARD_TRANSLATION_Y = 10f
private const val STAR_ROTATION_DURATION_BASE = 8000
private const val STAR_FLOAT_X_DURATION_BASE = 2000
private const val STAR_FLOAT_Y_DURATION_BASE = 2500
private const val STAR_PULSE_DURATION = 1500
private const val GLOW_SIZE = 220

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
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(CARD_ROTATION_DURATION, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rotation",
    )

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
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
    Box(
        modifier = Modifier
            .size(GLOW_SIZE.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SoftBlue.copy(alpha = 0.2f),
                            DarkBlue.copy(alpha = 0.1f),
                            Color.Transparent,
                        ),
                    ),
                )
            },
    )
}

@Composable
private fun CardStack(floatOffset: Float, rotation: Float, settings: CardDisplaySettings) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CARD_SPACING.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.offset(y = floatOffset.dp),
    ) {
        PreviewCard(Suit.Hearts, -BASE_ROTATION + rotation, 1f, settings)
        PreviewCard(Suit.Spades, BASE_ROTATION - rotation, 0f, settings, CARD_TRANSLATION_Y)
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
        suit = suit,
        rank = Rank.Ace,
        isFaceUp = true,
        isMatched = false,
        settings = settings,
        modifier = Modifier
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
    AnimatedStar(Modifier.offset(x = (-70).dp, y = (-60).dp).size(20.dp), 0)
    AnimatedStar(Modifier.offset(x = 80.dp, y = (-50).dp).size(16.dp), 500)
    AnimatedStar(Modifier.offset(x = (-80).dp, y = 40.dp).size(14.dp), 1000)
    AnimatedStar(Modifier.offset(x = 70.dp, y = 60.dp).size(18.dp), 200)
    AnimatedStar(Modifier.offset(x = 10.dp, y = (-85).dp).size(10.dp), 1500)
    AnimatedStar(Modifier.offset(x = (-30).dp, y = (-40).dp).size(6.dp), 800)
    AnimatedStar(Modifier.offset(x = 40.dp, y = 30.dp).size(8.dp), 1200)
}

@Composable
fun AnimatedStar(modifier: Modifier = Modifier, delayMillis: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_anim")

    // Floating movement for the star itself
    val floatX by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(STAR_FLOAT_X_DURATION_BASE + delayMillis % 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatX",
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(STAR_FLOAT_Y_DURATION_BASE + delayMillis % 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY",
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(STAR_PULSE_DURATION, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(STAR_PULSE_DURATION, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(STAR_ROTATION_DURATION_BASE + delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    Canvas(
        modifier = modifier
            .offset(x = floatX.dp, y = floatY.dp)
            .scale(scale)
            .alpha(alpha)
            .graphicsLayer { rotationZ = rotation },
    ) {
        val center = center
        val radius = size.minDimension / 2

        // Drawing a 4-pointed star (sparkle)
        val path = Path().apply {
            moveTo(center.x, center.y - radius)
            quadraticTo(center.x, center.y, center.x + radius, center.y)
            quadraticTo(center.x, center.y, center.x, center.y + radius)
            quadraticTo(center.x, center.y, center.x - radius, center.y)
            quadraticTo(center.x, center.y, center.x, center.y - radius)
            close()
        }

        // Outer glow
        drawPath(
            path = path,
            color = GoldenYellow.copy(alpha = 0.3f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Core
        drawPath(path, GoldenYellow)
    }
}
