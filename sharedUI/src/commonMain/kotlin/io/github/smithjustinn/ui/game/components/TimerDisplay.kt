package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import io.github.smithjustinn.theme.BonusGreen
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import io.github.smithjustinn.theme.TacticalRed
import io.github.smithjustinn.utils.formatTime

data class TimerState(
    val time: Long,
    val isLowTime: Boolean,
    val isCriticalTime: Boolean,
)

data class TimerFeedback(
    val showTimeGain: Boolean = false,
    val timeGainAmount: Int = 0,
    val showTimeLoss: Boolean = false,
    val timeLossAmount: Long = 0,
    val isMegaBonus: Boolean = false,
)

@Composable
fun TimerDisplay(
    state: TimerState,
    feedback: TimerFeedback,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    minimal: Boolean = false,
) {
    val timerColor = animateTimerColor(
        showTimeLoss = feedback.showTimeLoss,
        showTimeGain = feedback.showTimeGain,
        isMegaBonus = feedback.isMegaBonus,
        isLowTime = state.isLowTime,
        minimal = minimal,
    )

    val timerScale = calculateTimerScale(
        showTimeLoss = feedback.showTimeLoss,
        isCriticalTime = state.isCriticalTime,
        infiniteTransition = infiniteTransition,
    )

    if (minimal) {
        MinimalTimerDisplay(
            time = state.time,
            timerColor = timerColor,
            timerScale = timerScale,
            showTimeGain = feedback.showTimeGain,
            timeGainAmount = feedback.timeGainAmount,
            isMegaBonus = feedback.isMegaBonus,
            modifier = modifier,
        )
    } else {
        StandardTimerDisplay(
            time = state.time,
            timerColor = timerColor,
            timerScale = timerScale,
            showTimeGain = feedback.showTimeGain,
            timeGainAmount = feedback.timeGainAmount,
            showTimeLoss = feedback.showTimeLoss,
            timeLossAmount = feedback.timeLossAmount,
            isMegaBonus = feedback.isMegaBonus,
            isLowTime = state.isLowTime,
            compact = compact,
            modifier = modifier,
        )
    }
}

@Composable
private fun animateTimerColor(
    showTimeLoss: Boolean,
    showTimeGain: Boolean,
    isMegaBonus: Boolean,
    isLowTime: Boolean,
    minimal: Boolean,
): Color {
    val targetValue = when {
        showTimeLoss -> TacticalRed
        showTimeGain && isMegaBonus -> GoldenYellow
        showTimeGain -> BonusGreen
        isLowTime -> TacticalRed
        minimal -> Color.White
        else -> NeonCyan
    }
    
    val color by animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = if (showTimeGain || showTimeLoss) 100 else 500),
    )
    
    return color
}

@Composable
private fun calculateTimerScale(
    showTimeLoss: Boolean,
    isCriticalTime: Boolean,
    infiniteTransition: InfiniteTransition,
): Float {
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

    return when {
        showTimeLoss -> lossPulseScale
        isCriticalTime -> infinitePulseScale
        else -> 1f
    }
}

@Composable
private fun MinimalTimerDisplay(
    time: Long,
    timerColor: Color,
    timerScale: Float,
    showTimeGain: Boolean,
    timeGainAmount: Int,
    isMegaBonus: Boolean,
    modifier: Modifier = Modifier,
) {
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
}

@Composable
private fun StandardTimerDisplay(
    time: Long,
    timerColor: Color,
    timerScale: Float,
    showTimeGain: Boolean,
    timeGainAmount: Int,
    showTimeLoss: Boolean,
    timeLossAmount: Long,
    isMegaBonus: Boolean,
    isLowTime: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
        color = InactiveBackground.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLowTime || showTimeLoss) {
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
                    fontSize = (if (compact) 16.sp else 20.sp),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                ),
                modifier = Modifier
                    .scale(timerScale)
                    .padding(bottom = 2.dp), // Fine-tune vertical alignment
                color = timerColor,
                maxLines = 1,
            )

            TimeGainIndicator(
                showTimeGain = showTimeGain,
                timeGainAmount = timeGainAmount,
                isMegaBonus = isMegaBonus,
                compact = compact,
            )

            TimeLossIndicator(
                showTimeLoss = showTimeLoss,
                timeLossAmount = timeLossAmount,
                compact = compact,
            )
        }
    }
}

@Composable
private fun TimeGainIndicator(
    showTimeGain: Boolean,
    timeGainAmount: Int,
    isMegaBonus: Boolean,
    compact: Boolean,
) {
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
}

@Composable
private fun TimeLossIndicator(
    showTimeLoss: Boolean,
    timeLossAmount: Long,
    compact: Boolean,
) {
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
