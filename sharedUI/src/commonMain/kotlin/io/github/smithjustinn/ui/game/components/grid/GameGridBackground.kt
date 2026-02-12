package io.github.smithjustinn.ui.game.components.grid

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.theme.PokerTheme
import kotlin.random.Random

private const val FELT_TEXTURE_SEED = 42
private const val FELT_TEXTURE_POINT_COUNT = 1000

private const val SPOTLIGHT_X_START = 0.2f
private const val SPOTLIGHT_X_END = 0.8f
private const val SPOTLIGHT_Y_OFFSET_FRACTION = 0.3f
private const val SPOTLIGHT_DURATION_MS = 15000
private const val SPOTLIGHT_ALPHA = 0.6f
private const val SPOTLIGHT_RADIUS_MULTIPLIER = 0.9f

private const val VIGNETTE_ALPHA = 0.8f
private const val VIGNETTE_RADIUS_MULTIPLIER = 0.8f

private const val NOISE_ALPHA = 0.03f
private const val NOISE_STROKE_WIDTH = 2f

private const val BORDER_WHITE_ALPHA = 0.1f
private const val BORDER_BLACK_ALPHA = 0.5f

@Composable
internal fun GridBackground(
    theme: CardBackTheme,
    modifier: Modifier = Modifier,
) {
    val pokerColors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    val backgroundColor =
        when (theme) {
            CardBackTheme.GEOMETRIC -> Color(0xFF1A1A1A) // Dark/Modern
            CardBackTheme.CLASSIC -> pokerColors.background // Standard Green
            CardBackTheme.PATTERN -> Color(0xFF1E2A38) // Deep Blue
            CardBackTheme.POKER -> Color(0xFF2C1A1A) // Deep Maroon
        }

    val spotlightColor =
        when (theme) {
            CardBackTheme.GEOMETRIC -> Color(0xFF333333)
            CardBackTheme.CLASSIC -> pokerColors.feltGreenCenter
            CardBackTheme.PATTERN -> Color(0xFF2E3D4F)
            CardBackTheme.POKER -> Color(0xFF3D2525)
        }

    // Dynamic Spotlight Animation
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight")
    // Bolt: Use State<Float> instead of delegated property to defer reads to draw phase
    val spotlightXState =
        infiniteTransition.animateFloat(
            initialValue = SPOTLIGHT_X_START,
            targetValue = SPOTLIGHT_X_END,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = SPOTLIGHT_DURATION_MS, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "spotlightX",
        )

    Box(
        modifier =
            modifier.drawWithCache {
                // Bolt: Generate noise points only when size changes, avoiding allocation on every frame
                val random = Random(FELT_TEXTURE_SEED)
                val points =
                    List(FELT_TEXTURE_POINT_COUNT) {
                        Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                    }

                onDrawBehind {
                    drawFeltBackground(backgroundColor)
                    // Bolt: Read state inside draw phase to skip recomposition
                    drawSpotlight(spotlightXState.value, spotlightColor)
                    drawVignette()
                    drawNoiseTexture(points)
                    drawTableBorder(spacing.medium.toPx(), spacing.small.toPx())
                }
            },
    )
}

private fun DrawScope.drawFeltBackground(color: Color) {
    drawRect(color = color)
}

private fun DrawScope.drawSpotlight(
    spotlightX: Float,
    centerColor: Color,
) {
    val spotlightCenter = Offset(size.width * spotlightX, size.height * SPOTLIGHT_Y_OFFSET_FRACTION)
    drawRect(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        centerColor.copy(alpha = SPOTLIGHT_ALPHA),
                        Color.Transparent,
                    ),
                center = spotlightCenter,
                radius = size.maxDimension * SPOTLIGHT_RADIUS_MULTIPLIER,
            ),
    )
}

private fun DrawScope.drawVignette() {
    drawRect(
        brush =
            Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = VIGNETTE_ALPHA)),
                center = center,
                radius = size.maxDimension * VIGNETTE_RADIUS_MULTIPLIER,
            ),
    )
}

private fun DrawScope.drawNoiseTexture(points: List<Offset>) {
    drawPoints(
        points = points,
        pointMode = PointMode.Points,
        color = Color.White.copy(alpha = NOISE_ALPHA),
        strokeWidth = NOISE_STROKE_WIDTH,
    )
}

private fun DrawScope.drawTableBorder(
    mediumSpacing: Float,
    smallSpacing: Float,
) {
    // Chrome/Glass Rim
    drawRect(
        color = Color.White.copy(alpha = BORDER_WHITE_ALPHA),
        style = Stroke(width = mediumSpacing),
    )

    // Inner Table Shadow
    drawRect(
        color = Color.Black.copy(alpha = BORDER_BLACK_ALPHA),
        style = Stroke(width = smallSpacing),
        topLeft = Offset(mediumSpacing, mediumSpacing),
        size =
            size.copy(
                width = size.width - mediumSpacing * 2,
                height = size.height - mediumSpacing * 2,
            ),
    )
}
