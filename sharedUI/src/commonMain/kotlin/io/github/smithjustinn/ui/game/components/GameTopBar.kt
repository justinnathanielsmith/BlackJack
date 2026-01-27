package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.mute_content_description
import io.github.smithjustinn.resources.restart_content_description
import io.github.smithjustinn.resources.unmute_content_description
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.TacticalRed
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameTopBar(
    state: GameTopBarState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMuteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTimeAttack = state.mode == GameMode.TIME_ATTACK

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ).padding(
                    horizontal = if (state.compact) 16.dp else 24.dp,
                    vertical = if (state.compact) 8.dp else 12.dp,
                ),
        verticalArrangement = Arrangement.spacedBy(if (state.compact) 8.dp else 12.dp),
    ) {
        TopBarMainRow(
            state = state,
            onBackClick = onBackClick,
            onRestartClick = onRestartClick,
            onMuteClick = onMuteClick,
        )

        if (isTimeAttack && state.maxTime > 0) {
            TimeProgressBar(
                time = state.time,
                maxTime = state.maxTime,
                isLowTime = state.isLowTime,
                compact = state.compact,
            )
        }
    }
}

@Composable
private fun TopBarMainRow(
    state: GameTopBarState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMuteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BackButton(
            onClick = onBackClick,
            compact = state.compact,
        )

        TimerDisplay(
            infiniteTransition = rememberInfiniteTransition(),
            state =
                TimerState(
                    time = state.time,
                    isLowTime = state.isLowTime,
                    isCriticalTime = state.isCriticalTime,
                ),
            feedback =
                TimerFeedback(
                    showTimeGain = state.showTimeGain,
                    timeGainAmount = state.timeGainAmount,
                    showTimeLoss = state.showTimeLoss,
                    timeLossAmount = state.timeLossAmount,
                    isMegaBonus = state.isMegaBonus,
                ),
            layout = if (state.compact) TimerLayout.COMPACT else TimerLayout.STANDARD,
        )

        ControlButtons(
            isAudioEnabled = state.isAudioEnabled,
            onMuteClick = onMuteClick,
            onRestartClick = onRestartClick,
            compact = state.compact,
        )
    }
}

@Composable
private fun ControlButtons(
    isAudioEnabled: Boolean,
    onMuteClick: () -> Unit,
    onRestartClick: () -> Unit,
    compact: Boolean,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MuteButton(
            isAudioEnabled = isAudioEnabled,
            onClick = onMuteClick,
            compact = compact,
        )

        RestartButton(
            onClick = onRestartClick,
            compact = compact,
        )
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val size = if (compact) 40.dp else 48.dp
    // Let's use PokerButton style (Chip) but with Icon.

    // We can reuse PokerChip logic but passing custom content is not easy with current PokerChip.
    // I will implement it here using similar style.

    // Actually, let's use the Chip shape style but with the Icon.

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = PokerTheme.ChipRed,
        shadowElevation = 8.dp,
        modifier = modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Dashed border effect (simplified for icon button)
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = size.toPx() * 0.1f
                drawCircle(
                    color = Color.White,
                    style =
                        androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            pathEffect =
                                androidx.compose.ui.graphics.PathEffect
                                    .dashPathEffect(floatArrayOf(10f, 10f), 0f),
                        ),
                    radius = this.size.minDimension / 2 * 0.85f,
                )
            }

            Icon(
                AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.back_content_description),
                tint = PokerTheme.Gold,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp),
            )
        }
    }
}

@Composable
private fun RestartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = InactiveBackground.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier.size(if (compact) 40.dp else 48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                AppIcons.Restart,
                contentDescription = stringResource(Res.string.restart_content_description),
                tint = GoldenYellow,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp),
            )
        }
    }
}

@Composable
private fun MuteButton(
    isAudioEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = InactiveBackground.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier.size(if (compact) 40.dp else 48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (isAudioEnabled) AppIcons.VolumeUp else AppIcons.VolumeOff,
                contentDescription =
                    stringResource(
                        if (isAudioEnabled) {
                            Res.string.mute_content_description
                        } else {
                            Res.string.unmute_content_description
                        },
                    ),
                tint = if (isAudioEnabled) GoldenYellow else TacticalRed,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp),
            )
        }
    }
}

@Composable
private fun TimeProgressBar(
    time: Long,
    maxTime: Long,
    isLowTime: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val progress by animateFloatAsState(
        targetValue = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
    )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(if (compact) 4.dp else 8.dp)
                .clip(CircleShape)
                .background(InactiveBackground.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .shadow(
                        elevation = if (isLowTime) 0.dp else 12.dp,
                        shape = CircleShape,
                        ambientColor = GoldenYellow,
                        spotColor = GoldenYellow,
                        clip = false,
                    ).clip(CircleShape)
                    .background(
                        if (isLowTime) {
                            Brush.horizontalGradient(listOf(TacticalRed, TacticalRed.copy(alpha = 0.7f)))
                        } else {
                            Brush.horizontalGradient(listOf(GoldenYellow, GoldenYellow.copy(alpha = 0.7f)))
                        },
                    ),
        )
    }
}
