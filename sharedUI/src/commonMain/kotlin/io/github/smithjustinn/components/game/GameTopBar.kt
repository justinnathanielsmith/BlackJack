package io.github.smithjustinn.components.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.components.common.AppIcons
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import memory_match.sharedui.generated.resources.back_content_description
import memory_match.sharedui.generated.resources.best_score_label
import memory_match.sharedui.generated.resources.best_time_label
import memory_match.sharedui.generated.resources.combo_format
import memory_match.sharedui.generated.resources.peek_cards
import memory_match.sharedui.generated.resources.score_label
import memory_match.sharedui.generated.resources.time_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    score: Int,
    time: Long,
    bestScore: Int,
    bestTime: Long,
    combo: Int,
    onBackClick: () -> Unit,
    isPeeking: Boolean = false,
    mode: GameMode = GameMode.STANDARD,
    maxTime: Long = 0
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val isLowTime = isTimeAttack && time <= 10
    val isCriticalTime = isTimeAttack && time <= 5

    val timerColor by animateColorAsState(
        targetValue = if (isLowTime) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
        animationSpec = tween(durationMillis = 500)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val timerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCriticalTime) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.score_label, score),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (bestScore > 0) {
                                Text(
                                    text = stringResource(Res.string.best_score_label, bestScore),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Column {
                            Text(
                                text = stringResource(Res.string.time_label, formatTime(time)),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.scale(timerScale),
                                color = timerColor
                            )
                            if (bestTime > 0 && !isTimeAttack) {
                                Text(
                                    text = stringResource(Res.string.best_time_label, formatTime(bestTime)),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            },
            actions = {
                if (combo > 1) {
                    Text(
                        text = stringResource(Res.string.combo_format, combo),
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isPeeking) {
                    Icon(
                        imageVector = AppIcons.Visibility,
                        contentDescription = stringResource(Res.string.peek_cards),
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.back_content_description))
                }
            }
        )
        if (isTimeAttack && maxTime > 0) {
            val progress = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (isLowTime) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
