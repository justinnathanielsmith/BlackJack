package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import kotlin.math.roundToInt

@Composable
fun PlayingCard(
    suit: Suit,
    rank: Rank,
    isFaceUp: Boolean,
    isMatched: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    backColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isMatched -> 1.0f
            isHovered && !isFaceUp -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pulse"
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            repeat(3) {
                shakeOffset.animateTo(10f, tween(50))
                shakeOffset.animateTo(-10f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    val cardFaceColor = Color.White
    val suitColor = if (suit.isRed) Color(0xFFD32F2F) else Color(0xFF212121)
    
    val matchedGlowColor by animateColorAsState(
        targetValue = if (isMatched) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .width(80.dp)
            .height(110.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .graphicsLayer {
                rotationY = rotation
                scaleX = scale
                scaleY = scale
                cameraDistance = 15f * density
            }
            .shadow(
                elevation = if (isMatched) 0.dp else 6.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .drawBehind {
                if (isMatched) {
                    drawCircle(
                        color = matchedGlowColor,
                        radius = size.maxDimension * 0.7f,
                        center = center
                    )
                }
            }
    ) {
        Card(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (rotation <= 90f) cardFaceColor else backColor
            ),
            border = if (rotation <= 90f) {
                when {
                    isMatched -> BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                    isError -> BorderStroke(3.dp, Color(0xFFD32F2F))
                    else -> BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                }
            } else {
                BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (rotation <= 90f) {
                    CardFace(rank = rank, suit = suit, suitColor = suitColor)
                    if (isMatched) {
                        ShimmerEffect()
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        GeometricCardBack(backColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color
) {
    Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        // Top Left
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = rank.symbol,
                color = suitColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = suit.symbol,
                color = suitColor,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            )
        }

        // Center Suit
        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = 0.15f),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 60.sp),
            modifier = Modifier.align(Alignment.Center)
        )
        
        Text(
            text = rank.symbol,
            color = suitColor,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom Right
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { rotationZ = 180f }
        ) {
            Text(
                text = rank.symbol,
                color = suitColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = suit.symbol,
                color = suitColor,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            )
        }
    }
}

@Composable
fun ShimmerEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.0f),
        ),
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    )
}

@Composable
private fun GeometricCardBack(baseColor: Color) {
    val patternColor = Color.White.copy(alpha = 0.2f)
    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        drawRect(baseColor)
        
        val step = 16.dp.toPx()
        for (x in -step.toInt() until size.width.toInt() + step.toInt() step step.toInt()) {
            for (y in -step.toInt() until size.height.toInt() + step.toInt() step step.toInt()) {
                rotate(45f, Offset(x.toFloat(), y.toFloat())) {
                    drawRect(
                        color = patternColor,
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = androidx.compose.ui.geometry.Size(step / 2, step / 2),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
        
        // Inner border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
