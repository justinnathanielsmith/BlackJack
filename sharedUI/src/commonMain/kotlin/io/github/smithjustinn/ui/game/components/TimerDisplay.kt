package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.*
import io.github.smithjustinn.utils.formatTime

@Composable
fun TimerDisplay(
    time: Long,
    isLowTime: Boolean,
    isCriticalTime: Boolean,
    showTimeGain: Boolean,
    timeGainAmount: Int,
    showTimeLoss: Boolean,
    timeLossAmount: Long,
    isMegaBonus: Boolean,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    minimal: Boolean = false,
) {
    val timerColor by animateColorAsState(
        targetValue = when {
            showTimeLoss -> TacticalRed
            showTimeGain && isMegaBonus -> GoldenYellow
            showTimeGain -> BonusGreen
            isLowTime -> TacticalRed
            minimal -> Color.White
            else -> NeonCyan
        },
        animationSpec = tween(durationMillis = if (showTimeGain || showTimeLoss) 100 else 500),
    )

    val infinitePulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val lossPulseScale by animateFloatAsState(
        targetValue = if (showTimeLoss) 1.2f else 1f,
        animationSpec = if (showTimeLoss) {
            spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            spring(stiffness = Spring.StiffnessLow)
        },
    )

    val timerScale = when {
        showTimeLoss -> lossPulseScale
        isCriticalTime -> infinitePulseScale
        else -> 1f
    }

    if (minimal) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = formatTime(time),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                ),
                modifier = Modifier.scale(timerScale),
                color = timerColor,
            )

            AnimatedVisibility(
                visible = showTimeGain,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                Text(
                    text = "+${timeGainAmount}s",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isMegaBonus) GoldenYellow else BonusGreen,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
            color = InactiveBackground.copy(alpha = 0.4f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isLowTime ||
                    showTimeLoss
                ) {
                    TacticalRed.copy(alpha = 0.5f)
                } else {
                    Color.White.copy(alpha = 0.15f)
                },
            ),
            modifier = modifier.height(if (compact) 36.dp else 44.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = if (compact) 10.dp else 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = formatTime(time),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (compact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                    ),
                    modifier = Modifier.scale(timerScale),
                    color = timerColor,
                )

                AnimatedVisibility(
                    visible = showTimeGain,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 },
                ) {
                    Text(
                        text = "+${timeGainAmount}s",
                        style = if (compact) {
                            MaterialTheme.typography.labelSmall
                        } else {
                            MaterialTheme.typography.labelLarge
                        },
                        color = if (isMegaBonus) GoldenYellow else BonusGreen,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }

                AnimatedVisibility(
                    visible = showTimeLoss,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                ) {
                    Text(
                        text = "-${timeLossAmount}s",
                        style = if (compact) {
                            MaterialTheme.typography.labelSmall
                        } else {
                            MaterialTheme.typography.labelLarge
                        },
                        color = TacticalRed,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }
}
