package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.receipt_plus_amount_format
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val ReceiptInkColor = Color(0xFF2B2B2B)
private const val DOTTED_LINE_ALPHA = 0.5f
private const val DIVIDER_PADDING_DP = 4
private const val DOTTED_LINE_HEIGHT_DP = 2
private const val DOTTED_LINE_PADDING_DP = 8
private const val DASH_LONG = 10f
private const val DASH_GAP = 10f
private const val RECEIPT_EDGE_HEIGHT_DP = 12
private const val RECEIPT_TRIANGLE_COUNT = 20
private const val ROTATION_180_DEGREES = 180f
private const val SECONDS_PER_MINUTE = 60
private const val SCORE_TICK_DURATION_MS = 1500
private const val HIGH_SCORE_THRESHOLD = 100
private const val HIGH_SCORE_STEP = 10
private const val LOW_SCORE_STEP = 1

@Composable
fun ReceiptDivider(color: Color = ReceiptInkColor) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = DIVIDER_PADDING_DP.dp),
        thickness = 1.dp,
        color = color,
    )
}

@Composable
fun ReceiptDottedLine(color: Color = ReceiptInkColor) {
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(DOTTED_LINE_HEIGHT_DP.dp)
                .padding(vertical = DOTTED_LINE_PADDING_DP.dp),
    ) {
        drawLine(
            color = color.copy(alpha = DOTTED_LINE_ALPHA),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_LONG, DASH_GAP), 0f),
            strokeWidth = 2.dp.toPx(),
        )
    }
}

@Composable
fun ReceiptEdge(
    color: Color,
    isTop: Boolean,
) {
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(RECEIPT_EDGE_HEIGHT_DP.dp)
                .let { if (isTop) it.rotate(ROTATION_180_DEGREES) else it },
    ) {
        val width = size.width
        val triangleWidth = width / RECEIPT_TRIANGLE_COUNT
        val path =
            Path().apply {
                moveTo(0f, 0f)
                for (i in 0 until RECEIPT_TRIANGLE_COUNT) {
                    lineTo(i * triangleWidth + triangleWidth / 2, size.height)
                    lineTo((i + 1) * triangleWidth, 0f)
                }
                close()
            }
        drawPath(path, color = color)
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / SECONDS_PER_MINUTE
    val s = seconds % SECONDS_PER_MINUTE
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

@Composable
fun rememberAnimatedScore(
    score: Int,
    onScoreTick: () -> Unit,
): androidx.compose.runtime.State<Float> {
    val animatedScore = remember { Animatable(0f) }
    var lastRoundedScore by remember { mutableStateOf(0) }
    val scoreTickHandler by rememberUpdatedState(onScoreTick)

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = SCORE_TICK_DURATION_MS, easing = FastOutSlowInEasing),
        ) {
            handleScoreTick(value.roundToInt(), score, scoreTickHandler) { lastRoundedScore = it }
        }
    }
    return remember(animatedScore) { derivedStateOf { animatedScore.value } }
}

fun handleScoreTick(
    currentRounded: Int,
    targetScore: Int,
    scoreTickHandler: () -> Unit,
    updateLastScore: (Int) -> Unit,
) {
    val step = if (targetScore > HIGH_SCORE_THRESHOLD) HIGH_SCORE_STEP else LOW_SCORE_STEP
    if (currentRounded != 0 && (currentRounded % step == 0 || currentRounded == targetScore)) {
        scoreTickHandler()
        updateLastScore(currentRounded)
    }
}

@Composable
fun PayoutRow(
    label: String,
    amount: Int,
    color: Color = ReceiptInkColor,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                ),
            color = color,
        )
        Text(
            text = stringResource(Res.string.receipt_plus_amount_format, amount),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
            color = color,
        )
    }
}
