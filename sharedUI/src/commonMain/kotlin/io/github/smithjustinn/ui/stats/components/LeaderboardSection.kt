package io.github.smithjustinn.ui.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.Silver
import io.github.smithjustinn.utils.formatTime
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeaderboardSection(
    level: DifficultyLevel,
    entries: ImmutableList<LeaderboardEntry>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LeaderboardHeader(level)
        LeaderboardList(entries)
    }
}

@Composable
private fun LeaderboardHeader(level: DifficultyLevel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(level.nameRes).uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
            fontWeight = FontWeight.ExtraBold,
            color = GoldenYellow,
            letterSpacing = 1.sp,
        )
        Surface(
            color = GoldenYellow.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldenYellow.copy(alpha = 0.3f)),
        ) {
            Text(
                text = stringResource(Res.string.pairs_format, level.pairs),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                fontWeight = FontWeight.Bold,
                color = GoldenYellow,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun LeaderboardList(entries: ImmutableList<LeaderboardEntry>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = InactiveBackground.copy(alpha = 0.4f), // Felt darkness
        border =
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = GoldenYellow.copy(alpha = 0.15f), // Brass inlay
            ),
    ) {
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.no_stats_yet),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
        } else {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                entries.forEachIndexed { index, entry ->
                    LeaderboardRow(index + 1, entry)
                    if (index < entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = GoldenYellow.copy(alpha = 0.1f),
                        )
                    }
                }
            }
        }
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
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatMiniItem(
                label = stringResource(Res.string.stats_time_header),
                value = formatTime(entry.timeSeconds),
            )
            StatMiniItem(
                label = stringResource(Res.string.stats_moves_header),
                value = entry.moves.toString(),
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
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
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
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
            color = GoldenYellow.copy(alpha = 0.7f),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
