package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.ui.components.AppIcons

@Composable
fun DynamicIsland(
    timerContent: @Composable () -> Unit,
    scoreContent: @Composable () -> Unit,
    combo: Int,
    isGameOver: Boolean,
    finalScore: Int,
    bestScore: Int = 0,
    modifier: Modifier = Modifier,
    isMegaBonus: Boolean = false,
    isPeeking: Boolean = false
) {
    val isComboActive = combo > 1
    
    val islandWidth by animateDpAsState(
        targetValue = when {
            isGameOver -> 240.dp
            isComboActive && isPeeking -> 260.dp
            isComboActive -> 210.dp
            isPeeking -> 200.dp
            else -> 160.dp
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    val islandHeight by animateDpAsState(
        targetValue = if (isGameOver) 44.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    val comboPulseScale by animateFloatAsState(
        targetValue = if (isComboActive) 1.1f else 1f,
        animationSpec = if (isComboActive) {
            infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            snap()
        }
    )

    Surface(
        modifier = modifier
            .width(islandWidth)
            .height(islandHeight),
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1C1C1E),
                            Color(0xFF000000)
                        )
                    )
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = if (isGameOver) "SUMMARY" else if (isComboActive) "COMBO" else "TIMER",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300, delayMillis = 150)) + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f))
                }
            ) { state ->
                when (state) {
                    "SUMMARY" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "SCORE",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = finalScore.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }

                            if (bestScore > 0) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.2f)))
                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            AppIcons.Trophy,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(8.dp).padding(end = 2.dp)
                                        )
                                        Text(
                                            text = "BEST",
                                            color = Color.White.copy(alpha = 0.5f),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = bestScore.toString(),
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                    "COMBO" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isPeeking) {
                                Icon(
                                    imageVector = AppIcons.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF64B5F6),
                                    modifier = Modifier.size(16.dp)
                                )
                                VerticalDivider()
                            }

                            timerContent()
                            VerticalDivider()
                            scoreContent()
                            VerticalDivider()

                            Text(
                                text = "x$combo",
                                color = if (isMegaBonus) Color(0xFFFFD700) else Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.scale(comboPulseScale)
                            )
                        }
                    }
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isPeeking) {
                                Icon(
                                    imageVector = AppIcons.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF64B5F6),
                                    modifier = Modifier.size(16.dp)
                                )
                                VerticalDivider()
                            }

                            timerContent()
                            VerticalDivider()
                            scoreContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(12.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.2f))
    )
}
