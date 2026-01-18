package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.AppIcons
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.back_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameTopBar(
    score: Int,
    time: Long,
    bestScore: Int,
    combo: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPeeking: Boolean = false,
    mode: GameMode = GameMode.STANDARD,
    maxTime: Long = 0,
    showTimeGain: Boolean = false,
    timeGainAmount: Int = 0,
    showTimeLoss: Boolean = false,
    timeLossAmount: Long = 0,
    isMegaBonus: Boolean = false,
    compact: Boolean = false,
    isGameOver: Boolean = false
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
                horizontal = if (compact) 8.dp else 16.dp, 
                vertical = if (compact) 4.dp else 8.dp
            ),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BackButton(
                onClick = onBackClick, 
                compact = compact,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            DynamicIsland(
                timerContent = {
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
                        minimal = true
                    )
                },
                scoreContent = {
                    AnimatedScoreText(
                        score = score,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                },
                combo = combo,
                isGameOver = isGameOver,
                finalScore = score,
                bestScore = bestScore,
                isMegaBonus = isMegaBonus,
                isPeeking = isPeeking,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (isTimeAttack && maxTime > 0) {
            TimeProgressBar(
                time = time,
                maxTime = maxTime,
                isLowTime = isLowTime,
                compact = compact
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 4.dp,
        modifier = modifier.size(if (compact) 36.dp else 44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                AppIcons.ArrowBack,
                contentDescription = stringResource(Res.string.back_content_description),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp)
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
    compact: Boolean = false
) {
    val progress by animateFloatAsState(
        targetValue = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 4.dp else 8.dp)
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
