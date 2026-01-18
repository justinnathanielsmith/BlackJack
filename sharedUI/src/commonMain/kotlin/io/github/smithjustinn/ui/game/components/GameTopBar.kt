package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.AppIcons
import memory_match.sharedui.generated.resources.*
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
    compact: Boolean = false
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val isLowTime = isTimeAttack && time <= 10
    val isCriticalTime = isTimeAttack && time <= 5

    val infiniteTransition = rememberInfiniteTransition()

    // Removed the opaque Surface to make it immersive
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                horizontal = if (compact) 8.dp else 16.dp, 
                vertical = if (compact) 4.dp else 8.dp
            ),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
            ) {
                BackButton(onClick = onBackClick, compact = compact)
                
                if (compact) {
                    ComboBadge(
                        combo = combo,
                        isMegaBonus = isMegaBonus,
                        infiniteTransition = infiniteTransition,
                        compact = true
                    )
                    
                    if (isPeeking) {
                        PeekIndicator(isVisible = true)
                    }
                }
            }

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
                compact = compact
            )

            ScoreDisplay(
                score = score,
                bestScore = bestScore,
                compact = compact
            )
        }

        if (!compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComboBadge(
                    combo = combo,
                    isMegaBonus = isMegaBonus,
                    infiniteTransition = infiniteTransition,
                    compact = false
                )

                PeekIndicator(isVisible = isPeeking)
            }
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
private fun PeekIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally(),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            modifier = Modifier
                .padding(start = 4.dp)
                .size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.Visibility,
                    contentDescription = stringResource(Res.string.peek_cards),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
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
