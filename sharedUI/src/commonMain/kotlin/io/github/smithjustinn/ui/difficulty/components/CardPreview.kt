package io.github.smithjustinn.ui.difficulty.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.ui.game.components.PlayingCard

@Composable
fun CardPreview(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_preview_anim")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val containerHeight = maxHeight
        val cardSize = if (containerHeight < 150.dp) 80.dp else 120.dp
        
        Box(
            modifier = Modifier
                .size(if (containerHeight < 150.dp) 100.dp else 150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(y = floatOffset.dp)
        ) {
            PlayingCard(
                suit = Suit.Hearts,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = true,
                modifier = Modifier
                    .width(cardSize)
                    .offset(x = (cardSize.value / 4).dp)
                    .graphicsLayer { rotationZ = -15f + swayAngle }
            )
            PlayingCard(
                suit = Suit.Spades,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = true,
                modifier = Modifier
                    .width(cardSize)
                    .offset(x = (-cardSize.value / 4).dp)
                    .graphicsLayer { rotationZ = 15f + swayAngle }
            )
        }
    }
}
