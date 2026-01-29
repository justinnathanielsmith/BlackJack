package io.github.smithjustinn.ui.game.components

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
import kotlin.random.Random

private const val INITIAL_Y_OFFSET = 100f
private const val MAX_LIFE_BASE = 30f
private const val MAX_LIFE_RANDOM = 60f
private const val INITIAL_ALPHA_BASE = 0.2f
private const val INITIAL_ALPHA_RANDOM = 0.4f
private const val FADE_SPEED = 0.015f
private const val SIZE_BASE = 10f
private const val SIZE_RANDOM = 20f
private const val SPEED_BASE = 4f
private const val SPEED_RANDOM = 8f

private class SteamParticle(
    val size: Float,
    val speed: Float,
) {
    var x = 0f
    var y = 0f
    var alpha = 0f
    var life = 0f
    var maxLife = 0f

    fun reset(
        width: Float,
        height: Float,
    ) {
        x = Random.nextFloat() * width
        y = height + Random.nextFloat() * INITIAL_Y_OFFSET
        life = 0f
        maxLife = Random.nextFloat() * MAX_LIFE_RANDOM + MAX_LIFE_BASE
        alpha = Random.nextFloat() * INITIAL_ALPHA_RANDOM + INITIAL_ALPHA_BASE
    }

    fun update() {
        y -= speed
        life += 1f
        alpha -= FADE_SPEED
    }
}

@Composable
fun SteamEffect(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
) {
    if (!isVisible) return

    val particles =
        remember {
            List(particleCount) {
                SteamParticle(
                    size = Random.nextFloat() * SIZE_RANDOM + SIZE_BASE,
                    speed = Random.nextFloat() * SPEED_RANDOM + SPEED_BASE,
                )
            }
        }

    val frameState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Initial burst setup
            // We rely on the Canvas draw loop to init positions based on size
            while (true) {
                withFrameNanos { time ->
                    frameState.longValue = time
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val frame = frameState.longValue // Trigger redraw

        particles.forEach { p ->
            if (p.life == 0f && p.y == 0f) {
                p.reset(size.width, size.height)
            }

            if (p.alpha > 0f) {
                p.update()

                drawCircle(
                    color = Color.White.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}
