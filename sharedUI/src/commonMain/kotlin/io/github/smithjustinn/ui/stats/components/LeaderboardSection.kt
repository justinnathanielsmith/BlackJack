package io.github.smithjustinn.ui.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.no_stats_yet
import io.github.smithjustinn.resources.pairs_format
import io.github.smithjustinn.resources.score_label
import io.github.smithjustinn.resources.stats_moves_header
import io.github.smithjustinn.resources.stats_time_header
import io.github.smithjustinn.theme.Bronze
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.Silver
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.utils.formatTime
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeaderboardSection(
    level: DifficultyLevel,
    entries: ImmutableList<LeaderboardEntry>,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        title = stringResource(level.nameRes),
    ) {
        LeaderboardList(level, entries)
    }
}

@Composable
private fun LeaderboardList(
    level: DifficultyLevel,
    entries: ImmutableList<LeaderboardEntry>,
) {
    Column {
        LeaderboardInfoRow(level)

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.no_stats_yet),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        ),
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
        } else {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                entries.forEachIndexed { index, entry ->
                    LeaderboardRow(index + 1, entry)
                    if (index < entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardInfoRow(level: DifficultyLevel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.pairs_format, level.pairs),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
            fontWeight = FontWeight.Bold,
            color = GoldenYellow.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RankBadge(rank)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.score_label, entry.score),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.weight(0.6f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatMiniItem(
                label = stringResource(Res.string.stats_time_header),
                value = formatTime(entry.timeSeconds),
                modifier = Modifier.weight(1f)
            )
            StatMiniItem(
                label = stringResource(Res.string.stats_moves_header),
                value = entry.moves.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color =
            when (rank) {
                1 -> GoldenYellow.copy(alpha = 0.2f)
                2 -> Silver.copy(alpha = 0.2f)
                3 -> Bronze.copy(alpha = 0.2f)
                else -> Color.Black.copy(alpha = 0.3f)
            },
        border =
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color =
                    when (rank) {
                        1 -> GoldenYellow.copy(alpha = 0.8f)
                        2 -> Silver.copy(alpha = 0.8f)
                        3 -> Bronze.copy(alpha = 0.8f)
                        else -> Color.White.copy(alpha = 0.1f)
                    },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = rank.toString(),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                fontWeight = FontWeight.ExtraBold,
                color =
                    when (rank) {
                        1 -> GoldenYellow
                        2 -> Silver
                        3 -> Bronze
                        else -> Color.White.copy(alpha = 0.6f)
                    },
            )
        }
    }
}

@Composable
private fun StatMiniItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = label.uppercase(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
            color = GoldenYellow.copy(alpha = 0.7f),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
