package io.github.smithjustinn.ui.start.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.GoldenYellow

// Star Animation Durations
private const val STAR_ROTATION_DURATION_BASE = 8000
private const val STAR_FLOAT_X_DURATION_BASE = 2000
private const val STAR_FLOAT_Y_DURATION_BASE = 2500
private const val STAR_PULSE_DURATION = 1500

// Star Animation Values
private const val STAR_MAX_FLOAT_OFFSET = 4f
private const val STAR_DURATION_DELAY_MODULUS = 1000
private const val STAR_MIN_SCALE = 0.6f
private const val STAR_MAX_SCALE = 1.2f
private const val STAR_MIN_ALPHA = 0.4f
private const val STAR_MAX_ALPHA = 1f
private const val STAR_FULL_ROTATION_DEGREES = 360f

// Star Drawing
private const val STAR_GLOW_ALPHA = 0.3f
private const val STAR_STROKE_WIDTH_DP = 2

private data class StarAnimationValues(
    val floatX: Float,
    val floatY: Float,
    val scale: Float,
    val alpha: Float,
    val rotation: Float,
)

@Composable
private fun rememberStarFloatX(
    infiniteTransition: InfiniteTransition,
    delayMillis: Int,
) = infiniteTransition.animateFloat(
    initialValue = -STAR_MAX_FLOAT_OFFSET,
    targetValue = STAR_MAX_FLOAT_OFFSET,
    animationSpec =
        infiniteRepeatable(
            animation =
                tween(
                    durationMillis = STAR_FLOAT_X_DURATION_BASE + delayMillis % STAR_DURATION_DELAY_MODULUS,
                    easing = EaseInOutSine,
                ),
            repeatMode = RepeatMode.Reverse,
        ),
    label = "floatX",
)

@Composable
private fun rememberStarFloatY(
    infiniteTransition: InfiniteTransition,
    delayMillis: Int,
) = infiniteTransition.animateFloat(
    initialValue = -STAR_MAX_FLOAT_OFFSET,
    targetValue = STAR_MAX_FLOAT_OFFSET,
    animationSpec =
        infiniteRepeatable(
            animation =
                tween(
                    durationMillis = STAR_FLOAT_Y_DURATION_BASE + delayMillis % STAR_DURATION_DELAY_MODULUS,
                    easing = EaseInOutSine,
                ),
            repeatMode = RepeatMode.Reverse,
        ),
    label = "floatY",
)

@Composable
private fun rememberStarPulse(
    infiniteTransition: InfiniteTransition,
    delayMillis: Int,
    initial: Float,
    target: Float,
    label: String,
) = infiniteTransition.animateFloat(
    initialValue = initial,
    targetValue = target,
    animationSpec =
        infiniteRepeatable(
            animation = tween(STAR_PULSE_DURATION, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
    label = label,
)

@Composable
private fun rememberStarRotation(
    infiniteTransition: InfiniteTransition,
    delayMillis: Int,
) = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = STAR_FULL_ROTATION_DEGREES,
    animationSpec =
        infiniteRepeatable(
            animation = tween(STAR_ROTATION_DURATION_BASE + delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    label = "rotation",
)

@Composable
private fun rememberStarAnimationValues(delayMillis: Int): StarAnimationValues {
    val infiniteTransition = rememberInfiniteTransition(label = "star_anim")

    val floatX by rememberStarFloatX(infiniteTransition, delayMillis)
    val floatY by rememberStarFloatY(infiniteTransition, delayMillis)
    val scale by rememberStarPulse(infiniteTransition, delayMillis, STAR_MIN_SCALE, STAR_MAX_SCALE, "scale")
    val alpha by rememberStarPulse(infiniteTransition, delayMillis, STAR_MIN_ALPHA, STAR_MAX_ALPHA, "alpha")
    val rotation by rememberStarRotation(infiniteTransition, delayMillis)

    return StarAnimationValues(floatX, floatY, scale, alpha, rotation)
}

@Composable
fun AnimatedStar(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
) {
    val animValues = rememberStarAnimationValues(delayMillis)

    StarDrawing(
        modifier =
            modifier
                .offset(x = animValues.floatX.dp, y = animValues.floatY.dp)
                .scale(animValues.scale)
                .alpha(animValues.alpha)
                .graphicsLayer { rotationZ = animValues.rotation },
    )
}

@Composable
private fun StarDrawing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = center
        val radius = size.minDimension / 2

        // Drawing a 4-pointed star (sparkle)
        val path =
            Path().apply {
                moveTo(center.x, center.y - radius)
                quadraticTo(center.x, center.y, center.x + radius, center.y)
                quadraticTo(center.x, center.y, center.x, center.y + radius)
                quadraticTo(center.x, center.y, center.x - radius, center.y)
                quadraticTo(center.x, center.y, center.x, center.y - radius)
                close()
            }

        // Outer glow
        drawPath(
            path = path,
            color = GoldenYellow.copy(alpha = STAR_GLOW_ALPHA),
            style = Stroke(width = STAR_STROKE_WIDTH_DP.dp.toPx(), cap = StrokeCap.Round),
        )

        // Core
        drawPath(path, GoldenYellow)
    }
}
