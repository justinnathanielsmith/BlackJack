package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.utils.formatTime
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.final_score
import memory_match.sharedui.generated.resources.game_complete
import memory_match.sharedui.generated.resources.game_over
import memory_match.sharedui.generated.resources.moves_label
import memory_match.sharedui.generated.resources.play_again
import memory_match.sharedui.generated.resources.score_breakdown_title
import memory_match.sharedui.generated.resources.score_match_points
import memory_match.sharedui.generated.resources.score_move_bonus
import memory_match.sharedui.generated.resources.score_time_bonus
import memory_match.sharedui.generated.resources.time_label
import memory_match.sharedui.generated.resources.times_up
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun ResultsCard(
    isWon: Boolean,
    score: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.STANDARD
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes = when {
        isWon -> Res.string.game_complete
        isTimeAttack -> Res.string.times_up
        else -> Res.string.game_over
    }

    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = modifier
            .padding(24.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isWon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = if (isWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.final_score, animatedScore.value.roundToInt()),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isWon) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        StatItem(
                            label = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatItem(
                            label = stringResource(Res.string.moves_label, moves),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.score_breakdown_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                BreakdownRow(stringResource(Res.string.score_match_points, scoreBreakdown.matchPoints), MaterialTheme.colorScheme.onSurface)
                BreakdownRow(stringResource(Res.string.score_time_bonus, scoreBreakdown.timeBonus), if (scoreBreakdown.timeBonus > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
                BreakdownRow(stringResource(Res.string.score_move_bonus, scoreBreakdown.moveBonus), if (scoreBreakdown.moveBonus > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.play_again).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BreakdownRow(text: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
