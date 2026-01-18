package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.final_score_label
import memory_match.sharedui.generated.resources.moves_label
import memory_match.sharedui.generated.resources.time_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScoreBox(
    isWon: Boolean,
    score: Int,
    elapsedTimeSeconds: Long,
    moves: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(if (compact) 8.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.final_score_label).uppercase(),
                style = if (compact) {
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                } else {
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = score.toString(),
                style = if (compact) {
                    MaterialTheme.typography.headlineLarge
                } else {
                    MaterialTheme.typography.displayMedium
                }.copy(fontWeight = FontWeight.Black),
                color = if (isWon) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp),
                modifier = Modifier.padding(top = if (compact) 2.dp else 8.dp)
            ) {
                StatItem(
                    label = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    compact = compact
                )
                StatItem(
                    label = stringResource(Res.string.moves_label, moves),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = if (compact) 4.dp else 8.dp,
                vertical = if (compact) 2.dp else 4.dp
            ),
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
