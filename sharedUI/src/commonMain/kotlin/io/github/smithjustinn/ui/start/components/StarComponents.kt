package io.github.smithjustinn.ui.start.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.ModernGold

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
    val floatX: State<Float>,
    val floatY: State<Float>,
    val scale: State<Float>,
    val alpha: State<Float>,
    val rotation: State<Float>,
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

    val floatX = rememberStarFloatX(infiniteTransition, delayMillis)
    val floatY = rememberStarFloatY(infiniteTransition, delayMillis)
    val scale = rememberStarPulse(infiniteTransition, delayMillis, STAR_MIN_SCALE, STAR_MAX_SCALE, "scale")
    val alpha = rememberStarPulse(infiniteTransition, delayMillis, STAR_MIN_ALPHA, STAR_MAX_ALPHA, "alpha")
    val rotation = rememberStarRotation(infiniteTransition, delayMillis)

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
                // Bolt: Passing State<Float> and reading values inside graphicsLayer defers state reads
                // to the draw phase, preventing full recomposition on every frame.
                .graphicsLayer {
                    translationX =
                        animValues.floatX.value.dp
                            .toPx()
                    translationY =
                        animValues.floatY.value.dp
                            .toPx()
                    scaleX = animValues.scale.value
                    scaleY = animValues.scale.value
                    alpha = animValues.alpha.value
                    rotationZ = animValues.rotation.value
                },
    )
}

@Composable
private fun StarDrawing(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier.drawWithCache {
                // Bolt: Using drawWithCache prevents creating Path, Stroke,
                // and Color objects on every frame, reducing allocation churn.
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = size.minDimension / 2f

                // Drawing a 4-pointed star (sparkle)
                val path =
                    Path().apply {
                        moveTo(centerX, centerY - radius)
                        quadraticTo(centerX, centerY, centerX + radius, centerY)
                        quadraticTo(centerX, centerY, centerX, centerY + radius)
                        quadraticTo(centerX, centerY, centerX - radius, centerY)
                        quadraticTo(centerX, centerY, centerX, centerY - radius)
                        close()
                    }

                val strokeStyle = Stroke(width = STAR_STROKE_WIDTH_DP.dp.toPx(), cap = StrokeCap.Round)
                val glowColor = ModernGold.copy(alpha = STAR_GLOW_ALPHA)

                onDrawBehind {
                    // Outer glow
                    drawPath(path = path, color = glowColor, style = strokeStyle)
                    // Core
                    drawPath(path = path, color = ModernGold)
                }
            },
    )
}
