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
import io.github.smithjustinn.theme.PokerTheme
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
                modifier = Modifier.padding(vertical = PokerTheme.spacing.extraLarge).fillMaxWidth(),
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
            Column(modifier = Modifier.padding(top = PokerTheme.spacing.medium)) {
                entries.forEachIndexed { index, entry ->
                    LeaderboardRow(index + 1, entry)
                    if (index < entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = PokerTheme.spacing.small),
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
            style = PokerTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = PokerTheme.colors.goldenYellow.copy(alpha = 0.6f),
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
                .padding(horizontal = PokerTheme.spacing.medium, vertical = PokerTheme.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RankBadge(rank)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.score_label, entry.score),
                style = PokerTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }

        Row(
            modifier = Modifier.weight(WEIGHT_0_6),
            horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatMiniItem(
                label = stringResource(Res.string.stats_time_header),
                value = formatTime(entry.timeSeconds),
                modifier = Modifier.weight(1f),
            )
            StatMiniItem(
                label = stringResource(Res.string.stats_moves_header),
                value = entry.moves.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private const val WEIGHT_0_6 = 0.6f

private const val RANK_1 = 1
private const val RANK_2 = 2
private const val RANK_3 = 3
private const val BG_ALPHA_1 = 0.2f
private const val BG_ALPHA_2 = 0.2f
private const val BG_ALPHA_3 = 0.2f
private const val BG_ALPHA_DEFAULT = 0.3f
private const val BORDER_ALPHA_1 = 0.8f
private const val BORDER_ALPHA_2 = 0.8f
private const val BORDER_ALPHA_3 = 0.8f
private const val BORDER_ALPHA_DEFAULT = 0.1f
private const val TEXT_ALPHA_DEFAULT = 0.6f
private val ColorSilver = Color(0xFFC0C0C0)
private val ColorBronze = Color(0xFFCD7F32)

@Composable
private fun RankBadge(rank: Int) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color =
            when (rank) {
                RANK_1 -> PokerTheme.colors.goldenYellow.copy(alpha = BG_ALPHA_1)
                RANK_2 -> ColorSilver.copy(alpha = BG_ALPHA_2) // Silver
                RANK_3 -> ColorBronze.copy(alpha = BG_ALPHA_3) // Bronze
                else -> Color.Black.copy(alpha = BG_ALPHA_DEFAULT)
            },
        border =
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color =
                    when (rank) {
                        RANK_1 -> PokerTheme.colors.goldenYellow.copy(alpha = BORDER_ALPHA_1)
                        RANK_2 -> ColorSilver.copy(alpha = BORDER_ALPHA_2)
                        RANK_3 -> ColorBronze.copy(alpha = BORDER_ALPHA_3)
                        else -> Color.White.copy(alpha = BORDER_ALPHA_DEFAULT)
                    },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = rank.toString(),
                style = PokerTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color =
                    when (rank) {
                        RANK_1 -> PokerTheme.colors.goldenYellow
                        RANK_2 -> ColorSilver
                        RANK_3 -> ColorBronze
                        else -> Color.White.copy(alpha = TEXT_ALPHA_DEFAULT)
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
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = label.uppercase(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
            color = PokerTheme.colors.goldenYellow.copy(alpha = 0.7f),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}
