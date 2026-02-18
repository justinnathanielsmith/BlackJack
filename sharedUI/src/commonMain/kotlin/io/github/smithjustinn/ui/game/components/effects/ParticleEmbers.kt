package io.github.smithjustinn.ui.game.components.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.smithjustinn.theme.PokerTheme
import kotlin.random.Random

/**
 * A particle representing a single ember.
 */
private const val INITIAL_X = 0f
private const val INITIAL_Y = 0f
private const val OFF_SCREEN_THRESHOLD = -50f
private const val GLOW_ALPHA_FACTOR = 0.3f
private const val DRIFT_FACTOR = 0.5f
private const val DRIFT_OFFSET = 0.5f
private const val MAX_ALPHA = 0.8f
private const val SPEED_BASE = 1f
private const val SPEED_RANDOM = 3f
private const val SIZE_BASE = 4f
private const val SIZE_RANDOM = 12f
private const val MAX_LIFE_BASE = 300f
private const val MAX_LIFE_RANDOM = 200f

private const val EMBER_COLOR_ORANGE = 0xFFFF5722
private const val EMBER_COLOR_AMBER = 0xFFFFC107

/**
 * A particle representing a single ember.
 */
private class EmberParticle(
    val color: Color,
    val size: Float,
    val speed: Float,
) {
    var x = INITIAL_X
    var y = INITIAL_Y
    var alpha = 0f
    var phase = 0f // For alpha oscillation or lifecycle
    var drift = 0f // Horizontal drift

    // Lifecycle state
    var life = 0f
    var maxLife = 0f

    fun reset(
        width: Float,
        height: Float,
    ) {
        x = Random.nextFloat() * width
        y = height + size // Start just below screen
        life = 0f
        maxLife = Random.nextFloat() * MAX_LIFE_RANDOM + MAX_LIFE_BASE
        alpha = 0f
        drift = (Random.nextFloat() - DRIFT_OFFSET) * DRIFT_FACTOR
    }

    fun update() {
        y -= speed
        x += drift
        life += 1f

        // simple fade in/out
        val halfLife = maxLife / 2
        alpha = if (life < halfLife) {
            (life / halfLife).coerceIn(0f, 1f)
        } else {
            (1f - (life - halfLife) / halfLife).coerceIn(0f, 1f)
        } * MAX_ALPHA
    }

    fun isDead(): Boolean = y < OFF_SCREEN_THRESHOLD || life >= maxLife || x == INITIAL_X && y == INITIAL_Y
}

@Composable
fun ParticleEmbers(
    isHeatMode: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
) {
    val colors =
        listOf(
            PokerTheme.colors.tacticalRed,
            PokerTheme.colors.goldenYellow,
            Color(EMBER_COLOR_ORANGE),
            Color(EMBER_COLOR_AMBER),
        )

    val particles =
        remember {
            List(particleCount) {
                EmberParticle(
                    color = colors.random(),
                    size = Random.nextFloat() * SIZE_RANDOM + SIZE_BASE,
                    speed = Random.nextFloat() * SPEED_RANDOM + SPEED_BASE,
                )
            }
        }

    val frameState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(isHeatMode) {
        if (isHeatMode) {
            while (true) {
                withFrameNanos { time ->
                    frameState.longValue = time
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Trigger update on frame
        if (isHeatMode) {
            // Access frameState to trigger recomposition
            frameState.longValue

            particles.forEach { p ->
                if (p.isDead()) {
                    p.reset(size.width, size.height)
                    if (p.x == INITIAL_X && p.y == INITIAL_Y) {
                        p.y = Random.nextFloat() * size.height
                    }
                }

                p.update()

                drawCircle(
                    color = p.color.copy(alpha = p.alpha * GLOW_ALPHA_FACTOR),
                    radius = p.size,
                    center = Offset(p.x, p.y),
                )

                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size / 2,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}
