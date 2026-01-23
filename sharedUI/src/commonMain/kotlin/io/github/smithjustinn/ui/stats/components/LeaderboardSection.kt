package io.github.smithjustinn.ui.stats.components

import androidx.compose.foundation.layout.*
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
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.no_stats_yet
import memory_match.sharedui.generated.resources.pairs_format
import memory_match.sharedui.generated.resources.score_label
import memory_match.sharedui.generated.resources.stats_moves_header
import memory_match.sharedui.generated.resources.stats_time_header
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeaderboardSection(
    level: DifficultyLevel,
    entries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(level.nameRes).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                letterSpacing = 1.sp
            )
            Surface(
                color = NeonCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            ) {
                Text(
                    text = stringResource(Res.string.pairs_format, level.pairs),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = InactiveBackground.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f)
            )
        ) {
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.no_stats_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            } else {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    entries.forEachIndexed { index, entry ->
                        LeaderboardRow(index + 1, entry)
                        if (index < entries.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.White.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = when (rank) {
                1 -> Color(0xFFFFD700).copy(alpha = 0.2f) // Gold
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f) // Silver
                3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Bronze
                else -> Color.White.copy(alpha = 0.05f)
            },
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = when (rank) {
                    1 -> Color(0xFFFFD700).copy(alpha = 0.5f)
                    2 -> Color(0xFFC0C0C0).copy(alpha = 0.5f)
                    3 -> Color(0xFFCD7F32).copy(alpha = 0.5f)
                    else -> Color.White.copy(alpha = 0.1f)
                }
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> Color.White.copy(alpha = 0.6f)
                    }
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.score_label, entry.score),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatMiniItem(
                label = stringResource(Res.string.stats_time_header),
                value = formatTime(entry.timeSeconds)
            )
            StatMiniItem(
                label = stringResource(Res.string.stats_moves_header),
                value = entry.moves.toString()
            )
        }
    }
}

@Composable
private fun StatMiniItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan.copy(alpha = 0.6f),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
