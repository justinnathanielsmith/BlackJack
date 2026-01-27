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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.game_complete
import io.github.smithjustinn.resources.game_over
import io.github.smithjustinn.resources.play_again
import io.github.smithjustinn.resources.times_up
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val HIGH_SCORE_THRESHOLD = 100
private const val HIGH_SCORE_STEP = 10
private const val LOW_SCORE_STEP = 1
private const val INITIAL_SCALE = 0.8f

@Composable
fun ResultsCard(
    isWon: Boolean,
    score: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit = {},
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.STANDARD,
    onScoreTick: () -> Unit = {},
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes =
        when {
            isWon -> Res.string.game_complete
            isTimeAttack -> Res.string.times_up
            else -> Res.string.game_over
        }

    // Animation Logic extracted
    val animatedScore = rememberAnimatedScore(score, onScoreTick)
    val scale = rememberResultsScale()

    BoxWithConstraints(modifier = modifier.padding(horizontal = 24.dp)) {
        val isCompactHeight = maxHeight < 400.dp

        ResultsCardContent(
            isCompactHeight = isCompactHeight,
            scale = scale,
            isWon = isWon,
            titleRes = titleRes,
            animatedScore = animatedScore,
            elapsedTimeSeconds = elapsedTimeSeconds,
            moves = moves,
            scoreBreakdown = scoreBreakdown,
            onPlayAgain = onPlayAgain,
            onShareReplay = onShareReplay,
        )
    }
}

@Composable
private fun rememberAnimatedScore(
    score: Int,
    onScoreTick: () -> Unit,
): androidx.compose.runtime.State<Float> {
    val animatedScore = remember { Animatable(0f) }
    var lastRoundedScore by remember { mutableStateOf(0) }
    val scoreTickHandler by rememberUpdatedState(onScoreTick)

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        ) {
            val currentRounded = value.roundToInt()
            val step = if (score > HIGH_SCORE_THRESHOLD) HIGH_SCORE_STEP else LOW_SCORE_STEP
            if (currentRounded != lastRoundedScore && (currentRounded % step == 0 || currentRounded == score)) {
                scoreTickHandler()
                lastRoundedScore = currentRounded
            }
        }
    }
    return remember(animatedScore) { derivedStateOf { animatedScore.value } }
}

@Composable
private fun rememberResultsScale(): Float {
    val scale = remember { Animatable(INITIAL_SCALE) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
        )
    }
    return scale.value
}

@Composable
private fun ResultsCardContent(
    isCompactHeight: Boolean,
    scale: Float,
    isWon: Boolean,
    titleRes: org.jetbrains.compose.resources.StringResource,
    animatedScore: androidx.compose.runtime.State<Float>,
    elapsedTimeSeconds: Long,
    moves: Int,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .scale(scale)
                .widthIn(max = 550.dp),
        shape = RoundedCornerShape(24.dp),
        color = PokerTheme.colors.oakWood,
        border = BorderStroke(2.dp, PokerTheme.colors.goldenYellow.copy(alpha = 0.5f)), // Gold border
        shadowElevation = 16.dp,
    ) {
        val contentPadding = if (isCompactHeight) 12.dp else 24.dp
        val verticalSpacing = if (isCompactHeight) 8.dp else 16.dp
        val headerColor = if (isWon) PokerTheme.colors.goldenYellow else PokerTheme.colors.tacticalRed // Gold for win, Red for loss

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
                    .then(if (isCompactHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            Text(
                text = stringResource(titleRes).uppercase(),
                style =
                    if (isCompactHeight) {
                        MaterialTheme.typography.headlineSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                    } else {
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                    },
                color = headerColor,
                textAlign = TextAlign.Center,
            )

            if (isCompactHeight) {
                CompactResultsContent(
                    animatedScore = animatedScore.value.roundToInt(),
                    elapsedTimeSeconds = elapsedTimeSeconds,
                    moves = moves,
                    onPlayAgain = onPlayAgain,
                    onShareReplay = onShareReplay,
                )
            } else {
                StandardResultsContent(
                    animatedScore = animatedScore.value.roundToInt(),
                    elapsedTimeSeconds = elapsedTimeSeconds,
                    moves = moves,
                    scoreBreakdown = scoreBreakdown,
                    onPlayAgain = onPlayAgain,
                    onShareReplay = onShareReplay,
                )
            }
        }
    }
}

@Composable
private fun CompactResultsContent(
    animatedScore: Int,
    elapsedTimeSeconds: Long,
    moves: Int,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScoreBox(
            score = animatedScore,
            elapsedTimeSeconds = elapsedTimeSeconds,
            moves = moves,
            modifier = Modifier.weight(1f),
            compact = true,
        )

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PokerTheme.colors.tacticalRed,
                contentColor = Color.White
            ),
        ) {
            Text(
                text = stringResource(Res.string.play_again).uppercase(),
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                    ),
            )
        }
    }

    ResultsActionButtons(onShareReplay = onShareReplay, isCompact = true)
}

@Composable
private fun StandardResultsContent(
    animatedScore: Int,
    elapsedTimeSeconds: Long,
    moves: Int,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    ScoreBox(
        score = animatedScore,
        elapsedTimeSeconds = elapsedTimeSeconds,
        moves = moves,
    )

    ScoreBreakdownSection(scoreBreakdown = scoreBreakdown)

    Button(
        onClick = onPlayAgain,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PokerTheme.colors.tacticalRed,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        Text(
            text = stringResource(Res.string.play_again).uppercase(),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                ),
        )
    }

    ResultsActionButtons(onShareReplay = onShareReplay, isCompact = false)
}

@Composable
private fun ResultsActionButtons(
    onShareReplay: () -> Unit,
    isCompact: Boolean,
) {
    OutlinedButton(
        onClick = onShareReplay,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(if (isCompact) 48.dp else 56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PokerTheme.colors.goldenYellow),
        border = BorderStroke(1.dp, PokerTheme.colors.goldenYellow),
    ) {
        Icon(imageVector = AppIcons.Share, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SHARE REPLAY",
            style =
                if (isCompact) {
                    MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                } else {
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                    )
                },
        )
    }
}
