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
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import io.github.smithjustinn.theme.TacticalRed
import io.github.smithjustinn.ui.components.AppIcons
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.back_content_description
import memory_match.sharedui.generated.resources.mute_content_description
import memory_match.sharedui.generated.resources.restart_content_description
import memory_match.sharedui.generated.resources.unmute_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameTopBar(
    time: Long,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.STANDARD,
    maxTime: Long = 0,
    showTimeGain: Boolean = false,
    timeGainAmount: Int = 0,
    showTimeLoss: Boolean = false,
    timeLossAmount: Long = 0,
    isMegaBonus: Boolean = false,
    compact: Boolean = false,
    isAudioEnabled: Boolean = true,
    onMuteClick: () -> Unit = {},
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val isLowTime = isTimeAttack && time <= 10
    val isCriticalTime = isTimeAttack && time <= 5

    val infiniteTransition = rememberInfiniteTransition()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(
                horizontal = if (compact) 16.dp else 24.dp,
                vertical = if (compact) 8.dp else 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
    ) {
        // Main HUD Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left Controls
            BackButton(
                onClick = onBackClick,
                compact = compact,
            )

            // Center Area - Timer
            TimerDisplay(
                time = time,
                isLowTime = isLowTime,
                isCriticalTime = isCriticalTime,
                showTimeGain = showTimeGain,
                timeGainAmount = timeGainAmount,
                showTimeLoss = showTimeLoss,
                timeLossAmount = timeLossAmount,
                isMegaBonus = isMegaBonus,
                infiniteTransition = infiniteTransition,
                compact = compact,
                minimal = false,
            )

            // Right Controls
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

        if (isTimeAttack && maxTime > 0) {
            TimeProgressBar(
                time = time,
                maxTime = maxTime,
                isLowTime = isLowTime,
                compact = compact,
            )
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = InactiveBackground.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier.size(if (compact) 40.dp else 48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.back_content_description),
                tint = NeonCyan,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp),
            )
        }
    }
}

@Composable
private fun RestartButton(onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
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
                tint = NeonCyan,
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
                contentDescription = stringResource(
                    if (isAudioEnabled) Res.string.mute_content_description else Res.string.unmute_content_description,
                ),
                tint = if (isAudioEnabled) NeonCyan else TacticalRed,
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
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 4.dp else 8.dp)
            .clip(CircleShape)
            .background(InactiveBackground.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .shadow(
                    elevation = if (isLowTime) 0.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = NeonCyan,
                    spotColor = NeonCyan,
                    clip = false,
                )
                .clip(CircleShape)
                .background(
                    if (isLowTime) {
                        Brush.horizontalGradient(listOf(TacticalRed, TacticalRed.copy(alpha = 0.7f)))
                    } else {
                        Brush.horizontalGradient(listOf(NeonCyan, NeonCyan.copy(alpha = 0.7f)))
                    },
                ),
        )
    }
}
