package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private const val PARTICLES_PER_MATCH = 12
private const val BASE_DURATION_MS = 600
private const val RANDOM_DURATION_MS = 600
private const val DELAY_FACTOR_MS = 30
private const val RANDOM_DELAY_MS = 150
private const val BASE_SIZE = 8f
private const val RANDOM_SIZE = 8f
private const val WOBBLE_X_DP = 60
private const val WOBBLE_Y_DP = 30
private const val FADE_THRESHOLD = 0.8f
private const val SCALE_THRESHOLD_START = 0.2f
private const val SCALE_THRESHOLD_END = 0.8f
private const val MULTIPLIER_5 = 5f
private const val GLOW_ALPHA = 0.3f
private const val GLOW_RADIUS_FACTOR = 1.5f

@Composable
fun ScoreFlyingEffect(
    matchPositions: List<Offset>,
    targetPosition: Offset,
    modifier: Modifier = Modifier,
) {
    var points by remember { mutableStateOf(emptyList<FlyingPoint>()) }
    val timeSource = TimeSource.Monotonic

    // Trigger points when matchPositions changes and is not empty
    LaunchedEffect(matchPositions) {
        if (matchPositions.isNotEmpty()) {
            val now = timeSource.markNow()
            val newPoints =
                matchPositions.flatMap { pos ->
                    generateParticles(pos, targetPosition, now)
                }
            points = points + newPoints
        }
    }

    // Frame ticker to ensure smooth animation redraws
    var frameTick by remember { mutableStateOf(0L) }
    if (points.isNotEmpty()) {
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos { frameTick = it }
            }
        }
    }

    // Cleanup finished points
    SideEffect {
        val remaining =
            points.filter { point ->
                point.startTime.elapsedNow() < point.duration + point.delay
            }
        if (remaining.size != points.size) {
            points = remaining
        }
    }

    if (points.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        frameTick // trigger redraw

        points.forEach { point ->
            drawParticle(point)
        }
    }
}

private const val GOLD_COLOR = 0xFFFFD700
private const val BRASS_COLOR = 0xFFB5A642
private const val PALE_GOLD_COLOR = 0xFFFDE68A
private const val EASING_X1 = 0.2f
private const val EASING_Y1 = 0.0f
private const val EASING_X2 = 0.2f
private const val EASING_Y2 = 1.0f

private fun generateParticles(
    startPos: Offset,
    targetPos: Offset,
    now: TimeSource.Monotonic.ValueTimeMark,
): List<FlyingPoint> =
    List(PARTICLES_PER_MATCH) { i ->
        FlyingPoint(
            id = Random.nextLong(),
            startPos = startPos,
            targetPos = targetPos,
            startTime = now,
            duration = (BASE_DURATION_MS + Random.nextInt(RANDOM_DURATION_MS)).milliseconds,
            color =
                listOf(
                    Color(GOLD_COLOR),
                    Color(BRASS_COLOR),
                    Color.White,
                    Color(PALE_GOLD_COLOR),
                ).random(),
            size = BASE_SIZE + Random.nextFloat() * RANDOM_SIZE,
            delay = (i * DELAY_FACTOR_MS + Random.nextInt(RANDOM_DELAY_MS)).milliseconds,
        )
    }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawParticle(point: FlyingPoint) {
    val elapsedSinceStart = point.startTime.elapsedNow()
    val activeTime = elapsedSinceStart - point.delay

    if (activeTime.isPositive()) {
        val progress = (activeTime / point.duration).coerceIn(0.0, 1.0).toFloat()
        val easedProgress = CubicBezierEasing(EASING_X1, EASING_Y1, EASING_X2, EASING_Y2).transform(progress)

        // More pronounced arc/wobble
        val sideVelocity = if (point.id % 2 == 0L) 1f else -1f
        val wobbleX = sideVelocity * kotlin.math.sin(progress * kotlin.math.PI).toFloat() * WOBBLE_X_DP.dp.toPx()
        val wobbleY = -kotlin.math.sin(progress * kotlin.math.PI).toFloat() * WOBBLE_Y_DP.dp.toPx()

        val currentX = point.startPos.x + (point.targetPos.x - point.startPos.x) * easedProgress + wobbleX
        val currentY = point.startPos.y + (point.targetPos.y - point.startPos.y) * easedProgress + wobbleY

        val alpha = if (progress > FADE_THRESHOLD) (1f - progress) * MULTIPLIER_5 else 1f
        val scale = calculateScale(progress)

        // Draw a simple glow/trail
        drawCircle(
            color = point.color.copy(alpha = (alpha * GLOW_ALPHA).coerceIn(0f, 1f)),
            center = Offset(currentX, currentY),
            radius = point.size * scale * GLOW_RADIUS_FACTOR,
        )

        // The particle itself (looks like a gold coin/spark)
        drawCircle(
            color = point.color.copy(alpha = alpha.coerceIn(0f, 1f)),
            center = Offset(currentX, currentY),
            radius = point.size * scale / 2,
        )
    }
}

private fun calculateScale(progress: Float): Float =
    when {
        progress < SCALE_THRESHOLD_START -> progress * MULTIPLIER_5
        progress > SCALE_THRESHOLD_END -> (1f - progress) * MULTIPLIER_5
        else -> 1f
    }
