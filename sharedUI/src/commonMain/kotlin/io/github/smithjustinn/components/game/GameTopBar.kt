package io.github.smithjustinn.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.components.AppIcons
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import memory_match.sharedui.generated.resources.back_content_description
import memory_match.sharedui.generated.resources.best_score_label
import memory_match.sharedui.generated.resources.best_time_label
import memory_match.sharedui.generated.resources.combo_format
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
    onBackClick: () -> Unit
) {
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
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (bestTime > 0) {
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
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.back_content_description))
            }
        }
    )
}
