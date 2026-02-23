package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.best_score_label
import io.github.smithjustinn.resources.busted
import io.github.smithjustinn.resources.casino_header_title
import io.github.smithjustinn.resources.clean_number_format
import io.github.smithjustinn.resources.game_complete
import io.github.smithjustinn.resources.game_over
import io.github.smithjustinn.resources.high_roller_suite
import io.github.smithjustinn.resources.moves_label
import io.github.smithjustinn.resources.play_again
import io.github.smithjustinn.resources.score_combo_bonus_label
import io.github.smithjustinn.resources.score_daily_challenge_bonus
import io.github.smithjustinn.resources.score_double_down
import io.github.smithjustinn.resources.score_match_points_label
import io.github.smithjustinn.resources.score_move_efficiency
import io.github.smithjustinn.resources.score_time_bonus
import io.github.smithjustinn.resources.share_receipt
import io.github.smithjustinn.resources.time_label
import io.github.smithjustinn.resources.times_up
import io.github.smithjustinn.resources.total_payout
import io.github.smithjustinn.ui.components.ShopIcons
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.random.Random

private const val INITIAL_SCALE = 0.8f

// Receipt Colors
private val ReceiptPaperColor = Color(0xFFFDFBF7)
private val ReceiptInkColor = Color(0xFF2B2B2B)
private val ReceiptAccentColor = Color(0xFF8B0000) // Dark Red for key elements

private const val COMPACT_HEIGHT_THRESHOLD_DP = 450
private const val BARCODE_BAR_COUNT = 40
private const val BARCODE_THICKNESS_MODULO = 3
private const val BARCODE_THICKNESS_FACTOR_1 = 2
private const val BARCODE_THICKNESS_FACTOR_2 = 2
private const val BARCODE_SPACING_FACTOR = 2.5f
private const val BARCODE_WIDTH_FRACTION = 0.8f

@Composable
fun ResultsCard(
    isWon: Boolean,
    isBusted: Boolean = false,
    highScore: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit = {},
    modifier: Modifier = Modifier,
    mode: GameMode = GameMode.TIME_ATTACK,
    onScoreTick: () -> Unit = {},
) {
    val isTimeAttack = mode == GameMode.TIME_ATTACK
    val titleRes =
        when {
            isBusted -> Res.string.busted

            isWon -> Res.string.game_complete
            isTimeAttack -> Res.string.times_up
            else -> Res.string.game_over
        }

    val animatedScore = rememberAnimatedScore(scoreBreakdown.earnedCurrency, onScoreTick)
    val scale = rememberResultsScale()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val isCompactHeight = maxHeight < COMPACT_HEIGHT_THRESHOLD_DP.dp

        ResultsCardContent(
            scale = scale,
            isCompactHeight = isCompactHeight,
            lastMatchedScore = animatedScore.value.roundToInt(),
            highScore = highScore,
            titleRes = titleRes,
            isWon = isWon,
            scoreBreakdown = scoreBreakdown,
            moves = moves,
            elapsedTimeSeconds = elapsedTimeSeconds,
            onPlayAgain = onPlayAgain,
            onShareReplay = onShareReplay,
        )
    }
}

@Composable
private fun ResultsCardContent(
    scale: Float,
    isCompactHeight: Boolean,
    lastMatchedScore: Int,
    highScore: Int,
    titleRes: StringResource,
    isWon: Boolean,
    scoreBreakdown: ScoreBreakdown,
    moves: Int,
    elapsedTimeSeconds: Long,
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .scale(scale)
                .widthIn(max = 400.dp) // Receipt width
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
                .background(ReceiptPaperColor),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReceiptEdge(color = ReceiptPaperColor, isTop = true)

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 24.dp)
                        .then(if (isCompactHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CasinoHeader(titleRes, isWon)
                ReceiptDivider()
                PayoutSection(scoreBreakdown, moves, elapsedTimeSeconds)
                ReceiptDivider()
                TotalPayout(lastMatchedScore, highScore)
                ReceiptFooter(onPlayAgain, onShareReplay)
            }

            ReceiptEdge(color = ReceiptPaperColor, isTop = false)
        }
    }
}

@Composable
private fun CasinoHeader(
    titleRes: StringResource,
    isWon: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = ShopIcons.CasinoChip,
            contentDescription = null,
            tint = ReceiptInkColor,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.casino_header_title),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            color = ReceiptInkColor.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text =
                if (isWon) {
                    stringResource(Res.string.game_complete).uppercase()
                } else {
                    stringResource(titleRes).uppercase()
                },
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                ),
            color = if (isWon) ReceiptAccentColor else ReceiptInkColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.high_roller_suite),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                ),
            color = ReceiptInkColor.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun PayoutSection(
    scoreBreakdown: ScoreBreakdown,
    moves: Int,
    elapsedTimeSeconds: Long,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PayoutRow(stringResource(Res.string.score_match_points_label), scoreBreakdown.basePoints)
        if (scoreBreakdown.comboBonus > 0) {
            PayoutRow(stringResource(Res.string.score_combo_bonus_label), scoreBreakdown.comboBonus)
        }
        if (scoreBreakdown.doubleDownBonus > 0) {
            PayoutRow(stringResource(Res.string.score_double_down), scoreBreakdown.doubleDownBonus)
        }
        PayoutRow(stringResource(Res.string.score_time_bonus), scoreBreakdown.timeBonus)
        PayoutRow(stringResource(Res.string.score_move_efficiency), scoreBreakdown.moveBonus)
        if (scoreBreakdown.dailyChallengeBonus > 0) {
            PayoutRow(stringResource(Res.string.score_daily_challenge_bonus), scoreBreakdown.dailyChallengeBonus)
        }

        ReceiptDottedLine()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.moves_label, moves).uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = ReceiptInkColor.copy(alpha = 0.7f),
            )
            Text(
                text = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)).uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = ReceiptInkColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TotalPayout(
    animatedScore: Int,
    highScore: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.total_payout),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            color = ReceiptInkColor,
        )
        Text(
            text = stringResource(Res.string.clean_number_format, animatedScore),
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                ),
            color = ReceiptAccentColor,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.best_score_label, highScore).uppercase(),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
            color = ReceiptInkColor.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ReceiptFooter(
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BarcodeSimulation()
        Spacer(modifier = Modifier.height(4.dp))
        ResultsActions(onPlayAgain, onShareReplay)
    }
}

@Composable
private fun BarcodeSimulation() {
    Box(
        modifier =
            Modifier
                .height(30.dp)
                .fillMaxWidth(BARCODE_WIDTH_FRACTION),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width / BARCODE_BAR_COUNT
            var x = 0f
            while (x < size.width) {
                val thickness =
                    if ((x / barWidth).toInt() % BARCODE_THICKNESS_MODULO == 0) {
                        barWidth * BARCODE_THICKNESS_FACTOR_1
                    } else {
                        barWidth / BARCODE_THICKNESS_FACTOR_2
                    }
                if (Random.nextBoolean()) {
                    drawRect(
                        color = ReceiptInkColor,
                        topLeft = Offset(x, 0f),
                        size = Size(thickness, size.height),
                    )
                }
                x += barWidth * BARCODE_SPACING_FACTOR
            }
        }
    }
}

@Composable
private fun ResultsActions(
    onPlayAgain: () -> Unit,
    onShareReplay: () -> Unit,
) {
    Button(
        onClick = onPlayAgain,
        modifier = Modifier.fillMaxWidth(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ReceiptInkColor,
                contentColor = Color.White,
            ),
        shape = RoundedCornerShape(2.dp),
    ) {
        Text(
            text = stringResource(Res.string.play_again).uppercase(),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }

    OutlinedButton(
        onClick = onShareReplay,
        modifier = Modifier.fillMaxWidth(),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = ReceiptInkColor,
            ),
        border = BorderStroke(1.dp, ReceiptInkColor),
        shape = RoundedCornerShape(2.dp),
    ) {
        Text(
            text = stringResource(Res.string.share_receipt),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }
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
