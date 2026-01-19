package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
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
    mode: GameMode = GameMode.STANDARD,
    onScoreTick: () -> Unit = {}
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes = when {
        isWon -> Res.string.game_complete
        isTimeAttack -> Res.string.times_up
        else -> Res.string.game_over
    }

    val animatedScore = remember { Animatable(0f) }
    var lastRoundedScore by remember { mutableStateOf(0) }
    
    val scoreTickHandler by rememberUpdatedState(onScoreTick)

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) {
            val currentRounded = value.roundToInt()
            // Throttle haptics: every 10 points if score is high, or every point if score is low
            val step = if (score > 100) 10 else 1
            if (currentRounded != lastRoundedScore && (currentRounded % step == 0 || currentRounded == score)) {
                scoreTickHandler()
                lastRoundedScore = currentRounded
            }
        }
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

        Surface(
            modifier = Modifier
                .scale(scale.value)
                .widthIn(max = 550.dp),
            shape = RoundedCornerShape(24.dp),
            color = InactiveBackground.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shadowElevation = 16.dp
        ) {
            val contentPadding = if (isCompactHeight) 12.dp else 24.dp
            val verticalSpacing = if (isCompactHeight) 8.dp else 16.dp
            val headerColor = if (isWon) NeonCyan else MaterialTheme.colorScheme.error

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .then(if (isCompactHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                Text(
                    text = stringResource(titleRes).uppercase(),
                    style = if (isCompactHeight) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
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
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text(
                                text = stringResource(Res.string.play_again).uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                } else {
                    ScoreBox(
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
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.play_again).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
