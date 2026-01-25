package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CardState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val BASE_CARD_WIDTH = 64.dp
private val BASE_CARD_HEIGHT = 88.dp

private const val POP_IN_DURATION = 0.4f
private const val MAX_CELEBRATION_DURATION = 5.0f
private const val OFF_SCREEN_THRESHOLD = 200f
private const val FADE_OUT_STEP = 0.02f
private const val MAX_CELEBRATION_CARDS = 24
private const val ANGLE_SPREAD = 80f
private const val ANGLE_OFFSET = 130f
private const val MIN_SPEED = 20f
private const val SPEED_VARIATION = 20f
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val GRAVITY = 0.7f
private const val FRICTION = 0.992f
private const val DEGREES_TO_RADIANS = PI.toFloat() / 180f

/**
 * Internal state for a celebration card animation.
 * Features physics-based movement with gravity and rotation.
 *
 * Marked as @Stable because:
 * 1. All its mutable properties ([x], [y], [rotation], [scale], [alpha]) are backed
 *    by Compose [MutableState] objects.
 * 2. When these properties change, Compose is notified via the Snapshot system.
 * 3. The class instance itself remains stable while its internal state evolves.
 */
@Stable
private class CelebrationCard(
    val card: CardState,
    initialX: Float,
    initialY: Float,
    val vx0: Float,
    val vy0: Float,
    val vRot: Float,
    val targetScale: Float,
    val delaySeconds: Float,
) {
    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
    var vx = vx0
    var vy = vy0
    var rotation by mutableStateOf(0f)
    var scale by mutableStateOf(0f)
    var alpha by mutableStateOf(1f)

    fun update(gravity: Float, friction: Float, elapsedSeconds: Float, screenHeight: Float) {
        val activeTime = elapsedSeconds - delaySeconds
        if (activeTime < 0) return

        // Update physics
        x += vx
        y += vy
        vy += gravity
        vx *= friction
        vy *= friction
        rotation += vRot

        // Appearance animation (Pop in)
        scale = if (activeTime < POP_IN_DURATION) {
            (activeTime / POP_IN_DURATION) * targetScale
        } else {
            targetScale
        }

        // Exit animation (Fade out when falling off screen or after duration)
        if (y > screenHeight + OFF_SCREEN_THRESHOLD || activeTime > MAX_CELEBRATION_DURATION) {
            alpha = (alpha - FADE_OUT_STEP).coerceAtLeast(0f)
        }
    }
}

/**
 * A "surprise" celebration overlay that replaces the old bouncing cards with a
 * dynamic "Card Fountain" explosion. Cards shoot up from the bottom center
 * in a staggered sequence, creating a jackpot-like effect.
 */
@Composable
fun BouncingCardsOverlay(
    cards: List<CardState>,
    modifier: Modifier = Modifier,
    settings: CardDisplaySettings = CardDisplaySettings(),
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val cardWidthPx = with(density) { BASE_CARD_WIDTH.toPx() }

        // We use a list to track cards that are currently "in flight"
        val celebrationCards = remember { mutableStateListOf<CelebrationCard>() }

        // Initialize the celebration when cards are provided
        LaunchedEffect(cards) {
            if (celebrationCards.isEmpty() && cards.isNotEmpty()) {
                // Shuffle and take a subset to keep performance smooth but visual impact high
                cards.shuffled().take(MAX_CELEBRATION_CARDS).forEachIndexed { index, card ->
                    // Aim upwards with a slight spread
                    val angleDeg = Random.nextFloat() * ANGLE_SPREAD - ANGLE_OFFSET // Range approx -130 to -50 degrees
                    val radians = angleDeg * DEGREES_TO_RADIANS
                    val speed = MIN_SPEED + Random.nextFloat() * SPEED_VARIATION

                    celebrationCards.add(
                        CelebrationCard(
                            card = card,
                            initialX = (widthPx / 2f) - cardWidthPx / 2f,
                            // Start at the bottom
                            initialY = heightPx,
                            vx0 = cos(radians) * speed,
                            vy0 = sin(radians) * speed,
                            vRot = (Random.nextFloat() - 0.5f) * 12f,
                            targetScale = 0.7f + Random.nextFloat() * 0.5f,
                            delaySeconds = index * 0.08f, // Staggered launch for "fountain" feel
                        ),
                    )
                }
            }
        }

        PhysicsEngine(celebrationCards, heightPx)

        CelebrationCardsLayer(celebrationCards, settings)
    }
}

@Composable
private fun PhysicsEngine(celebrationCards: List<CelebrationCard>, heightPx: Float) {
    var startTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(celebrationCards.size) {
        if (celebrationCards.isNotEmpty()) {
            while (true) {
                withFrameNanos { frameTime ->
                    if (startTimeNanos == 0L) startTimeNanos = frameTime
                    val elapsedSeconds = (frameTime - startTimeNanos) / NANOS_PER_SECOND
                    celebrationCards.forEach { it.update(GRAVITY, FRICTION, elapsedSeconds, heightPx) }
                }
            }
        }
    }
}

@Composable
private fun CelebrationCardsLayer(celebrationCards: List<CelebrationCard>, settings: CardDisplaySettings) {
    celebrationCards.forEach { cCard ->
        if (cCard.alpha > 0f && cCard.scale > 0f) {
            key(cCard.card.id) {
                PlayingCard(
                    suit = cCard.card.suit,
                    rank = cCard.card.rank,
                    isFaceUp = true,
                    isMatched = true,
                    settings = settings,
                    modifier = Modifier
                        .size(BASE_CARD_WIDTH, BASE_CARD_HEIGHT)
                        .offset { IntOffset(cCard.x.roundToInt(), cCard.y.roundToInt()) }
                        .graphicsLayer {
                            rotationZ = cCard.rotation
                            scaleX = cCard.scale
                            scaleY = cCard.scale
                            alpha = cCard.alpha
                            shadowElevation = 8.dp.toPx()
                        },
                )
            }
        }
    }
}
