package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.CardSymbolTheme
import kotlin.math.*
import kotlin.random.Random

private val BASE_CARD_WIDTH = 64.dp
private val BASE_CARD_HEIGHT = 88.dp

/**
 * Internal state for a celebration card animation.
 * Features physics-based movement with gravity and rotation.
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
    val delaySeconds: Float
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
        scale = if (activeTime < 0.4f) {
            (activeTime / 0.4f) * targetScale
        } else {
            targetScale
        }

        // Exit animation (Fade out when falling off screen or after duration)
        if (y > screenHeight + 200f || activeTime > 5.0f) {
            alpha = (alpha - 0.02f).coerceAtLeast(0f)
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
    cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    areSuitsMultiColored: Boolean = false
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        
        val centerX = widthPx / 2f
        val cardWidthPx = with(density) { BASE_CARD_WIDTH.toPx() }

        // We use a list to track cards that are currently "in flight"
        val celebrationCards = remember { mutableStateListOf<CelebrationCard>() }
        var startTimeNanos by remember { mutableLongStateOf(0L) }

        // Initialize the celebration when cards are provided
        LaunchedEffect(cards) {
            if (celebrationCards.isEmpty() && cards.isNotEmpty()) {
                // Shuffle and take a subset to keep performance smooth but visual impact high
                cards.shuffled().take(24).forEachIndexed { index, card ->
                    // Aim upwards with a slight spread
                    val angleDeg = Random.nextFloat() * 80f - 130f // Range approx -130 to -50 degrees
                    val radians = angleDeg * (PI.toFloat() / 180f)
                    val speed = 20f + Random.nextFloat() * 20f
                    
                    celebrationCards.add(
                        CelebrationCard(
                            card = card,
                            initialX = centerX - cardWidthPx / 2f,
                            initialY = heightPx, // Start at the bottom
                            vx0 = cos(radians) * speed,
                            vy0 = sin(radians) * speed,
                            vRot = (Random.nextFloat() - 0.5f) * 12f,
                            targetScale = 0.7f + Random.nextFloat() * 0.5f,
                            delaySeconds = index * 0.08f // Staggered launch for "fountain" feel
                        )
                    )
                }
            }
        }

        // Animation loop
        LaunchedEffect(celebrationCards.size) {
            if (celebrationCards.isNotEmpty()) {
                while (true) {
                    withFrameNanos { frameTime ->
                        if (startTimeNanos == 0L) startTimeNanos = frameTime
                        val elapsedSeconds = (frameTime - startTimeNanos) / 1_000_000_000f
                        
                        celebrationCards.forEach { card ->
                            card.update(
                                gravity = 0.7f,
                                friction = 0.992f,
                                elapsedSeconds = elapsedSeconds,
                                screenHeight = heightPx
                            )
                        }
                    }
                }
            }
        }

        // Render the cards
        celebrationCards.forEach { cCard ->
            if (cCard.alpha > 0f && cCard.scale > 0f) {
                key(cCard.card.id) {
                    PlayingCard(
                        suit = cCard.card.suit,
                        rank = cCard.card.rank,
                        isFaceUp = true,
                        isMatched = true,
                        cardBackTheme = cardBackTheme,
                        cardSymbolTheme = cardSymbolTheme,
                        areSuitsMultiColored = areSuitsMultiColored,
                        modifier = Modifier
                            .size(BASE_CARD_WIDTH, BASE_CARD_HEIGHT)
                            .offset { IntOffset(cCard.x.roundToInt(), cCard.y.roundToInt()) }
                            .graphicsLayer {
                                rotationZ = cCard.rotation
                                scaleX = cCard.scale
                                scaleY = cCard.scale
                                alpha = cCard.alpha
                                // Add a slight shadow for depth as they fly
                                shadowElevation = 8.dp.toPx()
                            }
                    )
                }
            }
        }
    }
}
