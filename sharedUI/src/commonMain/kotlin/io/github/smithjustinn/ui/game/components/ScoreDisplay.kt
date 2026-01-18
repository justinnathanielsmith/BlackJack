package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.ui.components.AppIcons

@Composable
fun ScoreDisplay(
    score: Int,
    bestScore: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 6.dp,
        modifier = modifier.height(if (compact) 36.dp else 44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
        ) {
            AnimatedScoreText(score = score, compact = compact)

            if (bestScore > 0) {
                VerticalDivider(
                    modifier = Modifier.height(if (compact) 16.dp else 20.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                BestScoreBadge(bestScore = bestScore, compact = compact)
            }
        }
    }
}

@Composable
private fun AnimatedScoreText(
    score: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    AnimatedContent(
        targetState = score,
        modifier = modifier,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut()
                )
            } else {
                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> height } + fadeOut()
                )
            }.using(SizeTransform(clip = false))
        }
    ) { targetScore ->
        Text(
            text = targetScore.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = if (compact) 16.sp else 20.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun BestScoreBadge(
    bestScore: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            AppIcons.Trophy,
            contentDescription = null,
            modifier = Modifier.size(if (compact) 12.dp else 14.dp),
            tint = Color(0xFFFFD700)
        )
        Text(
            text = bestScore.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (compact) 8.sp else 10.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
    }
}
