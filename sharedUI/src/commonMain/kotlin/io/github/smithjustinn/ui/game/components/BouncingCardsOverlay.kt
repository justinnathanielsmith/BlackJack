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
import io.github.smithjustinn.domain.models.CardState
import kotlin.math.roundToInt
import kotlin.random.Random

private val BASE_CARD_WIDTH = 60.dp // Reduced from 80dp
private val BASE_CARD_HEIGHT = 80.dp // 3:4 aspect ratio (approx)

/**
 * Internal state for a bouncing card animation.
 */
@Stable
private class BouncingCard(
    val card: CardState,
    initialX: Float,
    initialY: Float,
    initialVx: Float,
    initialVy: Float,
    val vRot: Float = (Random.nextFloat() - 0.5f) * 5f,
    val scale: Float = 0.8f + Random.nextFloat() * 0.4f // Vary size slightly for better visual
) {
    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
    var vx = initialVx
    var vy = initialVy
    var rotation by mutableStateOf(0f)

    fun update(widthPx: Float, heightPx: Float, cardWidthPx: Float, cardHeightPx: Float) {
        x += vx
        y += vy
        rotation += vRot

        val maxX = (widthPx - cardWidthPx).coerceAtLeast(0f)
        val maxY = (heightPx - cardHeightPx).coerceAtLeast(0f)

        if (x <= 0f || x >= maxX) {
            vx = -vx
            x = x.coerceIn(0f, maxX)
        }
        if (y <= 0f || y >= maxY) {
            vy = -vy
            y = y.coerceIn(0f, maxY)
        }
    }
}

@Composable
fun BouncingCardsOverlay(
    cards: List<CardState>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        
        // Base size for bouncing cards
        val cardWidth = BASE_CARD_WIDTH
        val cardHeight = BASE_CARD_HEIGHT
        
        val bouncingCards = remember { mutableStateListOf<BouncingCard>() }

        // Initialize bouncing cards when the input list changes
        LaunchedEffect(cards) {
            if (bouncingCards.isEmpty() && cards.isNotEmpty()) {
                // If there are many cards (high difficulty), we might only want to bounce some of them
                // to keep the screen clear, but for toddler mode (12 cards) it should be fine.
                // We'll cap it at 12 cards anyway to prevent chaos on grandmaster mode.
                cards.take(12).forEach { card ->
                    // Calculate individual card dimensions for collision
                    val individualScale = 0.8f + Random.nextFloat() * 0.4f
                    val cW = with(density) { (cardWidth * individualScale).toPx() }
                    val cH = with(density) { (cardHeight * individualScale).toPx() }

                    bouncingCards.add(
                        BouncingCard(
                            card = card,
                            initialX = Random.nextFloat() * (widthPx - cW).coerceAtLeast(0f),
                            initialY = Random.nextFloat() * (heightPx - cH).coerceAtLeast(0f),
                            initialVx = (Random.nextFloat() - 0.5f) * 12f,
                            initialVy = (Random.nextFloat() - 0.5f) * 12f,
                            scale = individualScale
                        )
                    )
                }
            }
        }

        // Animation loop using withFrameNanos for smooth movement
        LaunchedEffect(widthPx, heightPx) {
            while (true) {
                withFrameNanos {
                    bouncingCards.forEach { bCard ->
                        val cW = with(density) { (cardWidth * bCard.scale).toPx() }
                        val cH = with(density) { (cardHeight * bCard.scale).toPx() }
                        bCard.update(widthPx, heightPx, cW, cH)
                    }
                }
            }
        }

        bouncingCards.forEach { bCard ->
            key(bCard.card.id) {
                PlayingCard(
                    suit = bCard.card.suit,
                    rank = bCard.card.rank,
                    isFaceUp = true,
                    isMatched = true,
                    modifier = Modifier
                        .size(cardWidth * bCard.scale, cardHeight * bCard.scale)
                        .offset { IntOffset(bCard.x.roundToInt(), bCard.y.roundToInt()) }
                        .graphicsLayer { rotationZ = bCard.rotation }
                )
            }
        }
    }
}
