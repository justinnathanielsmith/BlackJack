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
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.final_score_label
import io.github.smithjustinn.resources.moves_label
import io.github.smithjustinn.resources.time_label
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.utils.formatTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScoreBox(
    score: Int,
    elapsedTimeSeconds: Long,
    moves: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PokerTheme.colors.oakWood,
        border = BorderStroke(2.dp, PokerTheme.colors.goldenYellow.copy(alpha = 0.6f)), // Gold border
    ) {
        ScoreContent(
            score = score,
            elapsedTimeSeconds = elapsedTimeSeconds,
            moves = moves,
            compact = compact,
        )
    }
}

@Composable
private fun ScoreContent(
    score: Int,
    elapsedTimeSeconds: Long,
    moves: Int,
    compact: Boolean,
) {
    Column(
        modifier = Modifier.padding(if (compact) 12.dp else 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.final_score_label).uppercase(),
            style =
                if (compact) {
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    )
                } else {
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    )
                },
            color = PokerTheme.colors.goldenYellow,
            textAlign = TextAlign.Center,
        )

        Text(
            text = score.toString(),
            style =
                if (compact) {
                    MaterialTheme.typography.headlineLarge
                } else {
                    MaterialTheme.typography.displayMedium
                }.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        ScoreStatsRow(
            elapsedTimeSeconds = elapsedTimeSeconds,
            moves = moves,
            compact = compact,
        )
    }
}

@Composable
private fun ScoreStatsRow(
    elapsedTimeSeconds: Long,
    moves: Int,
    compact: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp),
        modifier = Modifier.padding(top = if (compact) 4.dp else 12.dp),
    ) {
        StatItem(
            label = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)),
            color = Color.White.copy(alpha = 0.9f),
            compact = compact,
        )
        StatItem(
            label = stringResource(Res.string.moves_label, moves),
            color = Color.White.copy(alpha = 0.9f),
            compact = compact,
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
    ) {
        Text(
            text = label,
            modifier =
                Modifier.padding(
                    horizontal = if (compact) 8.dp else 12.dp,
                    vertical = if (compact) 4.dp else 6.dp,
                ),
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Black,
        )
    }
}
