package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import io.github.smithjustinn.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A particle representing a single piece of confetti.
 */
private class Particle(val color: Color, val size: Float, angle: Double, speed: Float) {
    var x = 0f
    var y = 0f
    private var vx = cos(angle).toFloat() * speed
    private var vy = sin(angle).toFloat() * speed
    var alpha = 1f
    var rotation = Random.nextFloat() * 360f
    private val vRot = (Random.nextFloat() - 0.5f) * 20f

    fun update() {
        x += vx
        y += vy
        vy += 0.2f // Gravity
        alpha -= 0.01f
        rotation += vRot
    }
}

/**
 * A composable that displays a confetti burst effect.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param particleCount The number of particles to generate.
 * @param colors The list of colors to use for the particles.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 100,
    colors: List<Color> = listOf(
        ConfettiPink,
        ConfettiPurple,
        ConfettiBlue,
        BonusGreen,
        ConfettiYellow,
        ConfettiOrange,
    ),
) {
    val particles = remember { mutableStateListOf<Particle>() }
    val frameState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        // Create a burst of particles
        val newParticles = List(particleCount) {
            Particle(
                color = colors.random(),
                size = Random.nextFloat() * 10f + 8f,
                angle = Random.nextDouble(0.0, 2.0 * PI),
                speed = Random.nextFloat() * 12f + 4f,
            )
        }
        particles.addAll(newParticles)

        // Animation loop
        while (particles.isNotEmpty()) {
            withFrameNanos { time ->
                frameState.longValue = time
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.update()
                    if (p.alpha <= 0) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Reading frameState here triggers redraw of the Canvas on every frame
        // without recomposing the entire ConfettiEffect composable.
        @Suppress("UNUSED_VARIABLE")
        val frame = frameState.longValue

        val canvasCenter = center
        particles.forEach { p ->
            val position = canvasCenter + Offset(p.x, p.y)
            rotate(p.rotation, pivot = position) {
                drawRect(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    topLeft = position,
                    size = Size(p.size, p.size),
                )
            }
        }
    }
}
