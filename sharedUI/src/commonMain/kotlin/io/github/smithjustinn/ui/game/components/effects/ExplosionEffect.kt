package io.github.smithjustinn.ui.game.components.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import io.github.smithjustinn.theme.ModernGold
import io.github.smithjustinn.theme.TacticalRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ExplosionParticle(
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float,
)

@Composable
fun ExplosionEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    colors: List<Color> =
        listOf(
            ModernGold,
            TacticalRed,
            Color.Black,
            Color.White,
        ),
    centerOverride: Offset? = null,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(EXPLOSION_DURATION_MS, easing = LinearOutSlowInEasing),
        )
    }

    val particles =
        remember {
            List(particleCount) {
                val angle = Random.nextFloat() * 2 * PI
                val speed = Random.nextFloat() * MAX_PARTICLE_SPEED + MIN_PARTICLE_SPEED
                ExplosionParticle(
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    color = colors.random(),
                    size = Random.nextFloat() * MAX_PARTICLE_SIZE + MIN_PARTICLE_SIZE,
                    rotation = Random.nextFloat() * MAX_ROTATION_DEG,
                    rotationSpeed = Random.nextFloat() * MAX_ROTATION_SPEED_DIFF - MIN_ROTATION_SPEED_OFFSET,
                )
            }
        }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = centerOverride ?: Offset(size.width / 2, size.height / 2)

        particles.forEach { particle ->
            val currentX = center.x + particle.vx * progress.value * PARTICLE_DISTANCE_MULTIPLIER
            val currentY = center.y + particle.vy * progress.value * PARTICLE_DISTANCE_MULTIPLIER
            val alpha = (1f - progress.value).coerceIn(0f, 1f)

            rotate(
                degrees = particle.rotation + particle.rotationSpeed * progress.value * ROTATION_MULTIPLIER,
                pivot = Offset(currentX, currentY),
            ) {
                drawRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                    size = Size(particle.size, particle.size),
                )
            }
        }
    }
}

private const val EXPLOSION_DURATION_MS = 1000
private const val MAX_PARTICLE_SPEED = 15f
private const val MIN_PARTICLE_SPEED = 5f
private const val MAX_PARTICLE_SIZE = 10f
private const val MIN_PARTICLE_SIZE = 5f
private const val MAX_ROTATION_DEG = 360f
private const val MAX_ROTATION_SPEED_DIFF = 10f
private const val MIN_ROTATION_SPEED_OFFSET = 5f
private const val PARTICLE_DISTANCE_MULTIPLIER = 50f
private const val ROTATION_MULTIPLIER = 100f
