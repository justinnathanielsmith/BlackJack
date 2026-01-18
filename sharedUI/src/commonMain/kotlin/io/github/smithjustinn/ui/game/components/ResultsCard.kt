package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.game_complete
import memory_match.sharedui.generated.resources.game_over
import memory_match.sharedui.generated.resources.play_again
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

    BoxWithConstraints(modifier = modifier.padding(horizontal = 24.dp)) {
        val isCompactHeight = maxHeight < 400.dp

        Card(
            modifier = Modifier
                .scale(scale.value)
                .widthIn(max = 550.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            val contentPadding = if (isCompactHeight) 12.dp else 24.dp
            val verticalSpacing = if (isCompactHeight) 8.dp else 16.dp
            val headerColor = if (isWon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            val headerContainerColor = if (isWon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(headerContainerColor, MaterialTheme.colorScheme.surface)
                        )
                    )
                    .padding(contentPadding)
                    .then(if (isCompactHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = if (isCompactHeight) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    },
                    color = headerColor,
                    textAlign = TextAlign.Center
                )

                if (isCompactHeight) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScoreBox(
                            isWon = isWon,
                            score = animatedScore.value.roundToInt(),
                            elapsedTimeSeconds = elapsedTimeSeconds,
                            moves = moves,
                            modifier = Modifier.weight(1f),
                            compact = true
                        )
                        
                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = headerColor)
                        ) {
                            Text(
                                text = stringResource(Res.string.play_again).uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                } else {
                    ScoreBox(
                        isWon = isWon,
                        score = animatedScore.value.roundToInt(),
                        elapsedTimeSeconds = elapsedTimeSeconds,
                        moves = moves
                    )

                    ScoreBreakdownSection(scoreBreakdown = scoreBreakdown)

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = headerColor),
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
    }
}
