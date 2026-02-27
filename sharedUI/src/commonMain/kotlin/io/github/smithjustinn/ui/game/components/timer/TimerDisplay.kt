package io.github.smithjustinn.ui.game.components.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.timer_minus_seconds_format
import io.github.smithjustinn.resources.timer_plus_seconds_format
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.utils.formatTime
import org.jetbrains.compose.resources.stringResource

private const val COLOR_TRANSITION_DURATION_MS = 500
private const val PULSE_SCALE_TARGET = 1.15f

@Composable
fun TimerDisplay(
    infiniteTransition: InfiniteTransition,
    state: TimerState,
    feedback: TimerFeedback,
    modifier: Modifier = Modifier,
    layout: TimerLayout = TimerLayout.STANDARD,
) {
    val timerColorState =
        animateTimerColor(
            showTimeLoss = feedback.showTimeLoss,
            showTimeGain = feedback.showTimeGain,
            isMegaBonus = feedback.isMegaBonus,
            isLowTime = state.isLowTime,
            minimal = layout == TimerLayout.MINIMAL,
        )

    val timerScale =
        calculateTimerScale(
            infiniteTransition = infiniteTransition,
            showTimeLoss = feedback.showTimeLoss,
            isCriticalTime = state.isCriticalTime,
        )

    val visuals =
        TimerVisuals(
            color = timerColorState,
            scale = timerScale,
            layout = layout,
        )

    if (layout == TimerLayout.MINIMAL) {
        MinimalTimerDisplay(
            state = state,
            feedback = feedback,
            visuals = visuals,
            modifier = modifier,
        )
    } else {
        StandardTimerDisplay(
            state = state,
            feedback = feedback,
            visuals = visuals,
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
): State<Color> {
    val targetValue =
        when {
            showTimeLoss -> PokerTheme.colors.tacticalRed
            showTimeGain && isMegaBonus -> PokerTheme.colors.goldenYellow
            showTimeGain -> PokerTheme.colors.bonusGreen
            isLowTime -> PokerTheme.colors.tacticalRed
            minimal -> Color.White
            else -> PokerTheme.colors.brass
        }

    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = if (showTimeGain || showTimeLoss) 100 else COLOR_TRANSITION_DURATION_MS),
    )
}

@Composable
private fun calculateTimerScale(
    infiniteTransition: InfiniteTransition,
    showTimeLoss: Boolean,
    isCriticalTime: Boolean,
): State<Float> {
    val infinitePulseScaleState =
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = PULSE_SCALE_TARGET,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(COLOR_TRANSITION_DURATION_MS, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

    val lossPulseScaleState =
        animateFloatAsState(
            targetValue = if (showTimeLoss) 1.2f else 1f,
            animationSpec =
                if (showTimeLoss) {
                    spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
                } else {
                    spring(stiffness = Spring.StiffnessLow)
                },
        )

    return remember(showTimeLoss, isCriticalTime) {
        derivedStateOf {
            when {
                showTimeLoss -> lossPulseScaleState.value
                isCriticalTime -> infinitePulseScaleState.value
                else -> 1f
            }
        }
    }
}

@Composable
private fun MinimalTimerDisplay(
    state: TimerState,
    feedback: TimerFeedback,
    visuals: TimerVisuals,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        TimerDigits(
            time = state.time,
            visuals = visuals,
            fontSize = 14.sp,
        )

        AnimatedVisibility(
            visible = feedback.showTimeGain,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
        ) {
            Text(
                text = stringResource(Res.string.timer_plus_seconds_format, feedback.timeGainAmount),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color =
                    if (feedback.isMegaBonus) {
                        PokerTheme.colors.goldenYellow
                    } else {
                        PokerTheme.colors.bonusGreen
                    },
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun StandardTimerDisplay(
    state: TimerState,
    feedback: TimerFeedback,
    visuals: TimerVisuals,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(if (visuals.layout == TimerLayout.COMPACT) 16.dp else 24.dp),
        color = PokerTheme.colors.oakWood,
        border =
            BorderStroke(
                width = 2.dp,
                color =
                    if (state.isLowTime || feedback.showTimeLoss) {
                        PokerTheme.colors.tacticalRed
                    } else {
                        PokerTheme.colors.goldenYellow.copy(alpha = 0.5f)
                    },
            ),
        modifier = modifier.height(if (visuals.layout == TimerLayout.COMPACT) 36.dp else 44.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (visuals.layout == TimerLayout.COMPACT) 10.dp else 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            TimerDigits(
                time = state.time,
                visuals = visuals,
                fontSize = if (visuals.layout == TimerLayout.COMPACT) 16.sp else 20.sp,
                modifier = Modifier.padding(bottom = 2.dp),
            )

            TimeGainIndicator(
                showTimeGain = feedback.showTimeGain,
                timeGainAmount = feedback.timeGainAmount,
                isMegaBonus = feedback.isMegaBonus,
                compact = visuals.layout == TimerLayout.COMPACT,
            )

            TimeLossIndicator(
                showTimeLoss = feedback.showTimeLoss,
                timeLossAmount = feedback.timeLossAmount,
                compact = visuals.layout == TimerLayout.COMPACT,
            )
        }
    }
}

@Composable
private fun TimerDigits(
    time: Long,
    visuals: TimerVisuals,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = formatTime(time),
        style =
            MaterialTheme.typography.titleLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
            ),
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = visuals.scale.value
                    scaleY = visuals.scale.value
                },
        color = visuals.color.value,
        maxLines = 1,
    )
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
            text = stringResource(Res.string.timer_plus_seconds_format, timeGainAmount),
            style =
                if (compact) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.labelLarge
                },
            color = if (isMegaBonus) PokerTheme.colors.goldenYellow else PokerTheme.colors.bonusGreen,
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
            text = stringResource(Res.string.timer_minus_seconds_format, timeLossAmount),
            style =
                if (compact) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.labelLarge
                },
            color = PokerTheme.colors.tacticalRed,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
