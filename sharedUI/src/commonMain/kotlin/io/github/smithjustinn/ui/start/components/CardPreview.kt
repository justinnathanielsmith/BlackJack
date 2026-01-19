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
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.ui.game.components.PlayingCard

@Composable
fun CardPreview(
    modifier: Modifier = Modifier,
    cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_preview_anim")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Background Glow Effect
        Box(
            modifier = Modifier
                .size(220.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF60A5FA).copy(alpha = 0.2f),
                                Color(0xFF1E3A8A).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )
        
        // Cards
        Row(
            horizontalArrangement = Arrangement.spacedBy((-55).dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(y = floatOffset.dp)
        ) {
            PlayingCard(
                suit = Suit.Hearts,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = true,
                cardBackTheme = cardBackTheme,
                cardSymbolTheme = cardSymbolTheme,
                modifier = Modifier
                    .width(110.dp)
                    .graphicsLayer { 
                        rotationZ = -12f + rotation 
                        scaleX = 1f
                        scaleY = 1f
                    }
            )
            PlayingCard(
                suit = Suit.Spades,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = true,
                cardBackTheme = cardBackTheme,
                cardSymbolTheme = cardSymbolTheme,
                modifier = Modifier
                    .width(110.dp)
                    .graphicsLayer { 
                        rotationZ = 12f - rotation 
                        scaleX = 1f
                        scaleY = 1f
                        translationY = 10f
                    }
            )
        }
        
        // Stars/Sparkles (Placed after Cards to be on top)
        // Positioned relative to center using offsets
        AnimatedStar(Modifier.offset(x = (-70).dp, y = (-60).dp).size(20.dp), 0)
        AnimatedStar(Modifier.offset(x = 80.dp, y = (-50).dp).size(16.dp), 500)
        AnimatedStar(Modifier.offset(x = (-80).dp, y = 40.dp).size(14.dp), 1000)
        AnimatedStar(Modifier.offset(x = 70.dp, y = 60.dp).size(18.dp), 200)
        AnimatedStar(Modifier.offset(x = 10.dp, y = (-85).dp).size(10.dp), 1500)
        
        // Some tiny ones
        AnimatedStar(Modifier.offset(x = (-30).dp, y = (-40).dp).size(6.dp), 800)
        AnimatedStar(Modifier.offset(x = 40.dp, y = 30.dp).size(8.dp), 1200)
    }
}

@Composable
fun AnimatedStar(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_anim")
    
    // Floating movement for the star itself
    val floatX by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + delayMillis % 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + delayMillis % 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000 + delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier
            .offset(x = floatX.dp, y = floatY.dp)
            .scale(scale)
            .alpha(alpha)
            .graphicsLayer { rotationZ = rotation }
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
            color = Color(0xFFFFD700).copy(alpha = 0.3f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Core
        drawPath(path, Color(0xFFFFD700))
    }
}
