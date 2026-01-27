package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.score_breakdown_title
import io.github.smithjustinn.resources.score_match_points
import io.github.smithjustinn.resources.score_move_bonus
import io.github.smithjustinn.resources.score_time_bonus
import io.github.smithjustinn.theme.BonusGreen
import org.jetbrains.compose.resources.stringResource

private val BonusColor = BonusGreen

@Composable
fun ScoreBreakdownSection(
    scoreBreakdown: ScoreBreakdown,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color =
            io.github.smithjustinn.theme.MemoryMatchTheme.colors.inactiveBackground
                .copy(alpha = 0.4f),
        border =
            BorderStroke(
                1.dp,
                io.github.smithjustinn.theme.MemoryMatchTheme.colors.goldenYellow
                    .copy(alpha = 0.3f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.score_breakdown_title).uppercase(),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                fontWeight = FontWeight.Black,
                color = io.github.smithjustinn.theme.MemoryMatchTheme.colors.goldenYellow,
            )

            BreakdownRow(
                label = stringResource(Res.string.score_match_points),
                value = scoreBreakdown.matchPoints.toString(),
                color = Color.White,
            )
            BreakdownRow(
                label = stringResource(Res.string.score_time_bonus),
                value = if (scoreBreakdown.timeBonus > 0) "+${scoreBreakdown.timeBonus}" else "0",
                color = if (scoreBreakdown.timeBonus > 0) BonusColor else Color.White,
            )
            BreakdownRow(
                label = stringResource(Res.string.score_move_bonus),
                value = if (scoreBreakdown.moveBonus > 0) "+${scoreBreakdown.moveBonus}" else "0",
                color = if (scoreBreakdown.moveBonus > 0) BonusColor else Color.White,
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFeatureSettings = "tnum",
                ),
            color = color,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}
