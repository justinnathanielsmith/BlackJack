package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameTopBar(
    score: Int,
    time: Long,
    bestScore: Int,
    combo: Int,
    onBackClick: () -> Unit,
    isPeeking: Boolean = false,
    mode: GameMode = GameMode.STANDARD,
    maxTime: Long = 0,
    showTimeGain: Boolean = false,
    timeGainAmount: Int = 0,
    isMegaBonus: Boolean = false
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val isLowTime = isTimeAttack && time <= 10
    val isCriticalTime = isTimeAttack && time <= 5

    val timerColor by animateColorAsState(
        targetValue = when {
            showTimeGain && isMegaBonus -> Color(0xFFFFD700)
            showTimeGain -> Color(0xFF4CAF50)
            isLowTime -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = if (showTimeGain) 100 else 500)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val timerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCriticalTime) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button - Circular and clean
            Surface(
                onClick = onBackClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        AppIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.back_content_description),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Timer Display - Pill shape with subtle border when low
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = if (isLowTime) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null,
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formatTime(time),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.scale(timerScale),
                        color = timerColor
                    )

                    AnimatedVisibility(
                        visible = showTimeGain,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { -it / 2 }
                    ) {
                        Text(
                            text = "+${timeGainAmount}s",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isMegaBonus) Color(0xFFFFD700) else Color(0xFF4CAF50),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            // Score Display - Modern "Badge" style
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedContent(
                            targetState = score,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                        slideOutVertically { height -> -height } + fadeOut())
                                } else {
                                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                        slideOutVertically { height -> height } + fadeOut())
                                }.using(SizeTransform(clip = false))
                            }
                        ) { targetScore ->
                            Text(
                                text = targetScore.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (bestScore > 0) {
                        VerticalDivider(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                AppIcons.Trophy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFD700) // Gold for trophy
                            )
                            Text(
                                text = bestScore.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Combo and Peek Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = combo > 1,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                val comboScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Surface(
                    color = if (isMegaBonus) Color(0xFFFFD700) else MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .scale(comboScale)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(Res.string.combo_format, combo),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        ),
                        color = if (isMegaBonus) Color.Black else MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            AnimatedVisibility(
                visible = isPeeking,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.Visibility,
                            contentDescription = stringResource(Res.string.peek_cards),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Animated Progress Bar for Time Attack
        if (isTimeAttack && maxTime > 0) {
            val progress by animateFloatAsState(
                targetValue = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                if (isLowTime) {
                                    listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
                                } else {
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                }
                            )
                        )
                )
            }
        }
    }
}
