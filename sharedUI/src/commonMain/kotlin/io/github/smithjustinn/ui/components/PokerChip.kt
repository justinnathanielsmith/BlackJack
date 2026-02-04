package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.PokerTheme
import androidx.compose.ui.graphics.Shadow

private const val ELEVATION_PRESSED = 2
private const val ELEVATION_SELECTED = 12
private const val ELEVATION_DEFAULT = 4
private const val GLOW_ALPHA_SELECTED = 0.5f
private const val GLOW_RADIUS_FACTOR = 0.8f

@Composable
fun PokerChip(
    text: String,
    contentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    chipSize: Dp = 64.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val chipAnimations = rememberPokerChipAnimations(isSelected, interactionSource)
    val elevation = chipAnimations.elevation
    val scale = chipAnimations.scale
    val glowAlpha = chipAnimations.glowAlpha
    val glimmerBrush = if (isSelected) rememberGlimmerBrush() else null

    val glowColor = PokerTheme.colors.goldenYellow

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        PokerChipContent(
            chipSize = chipSize,
            scale = scale,
            glowAlpha = glowAlpha,
            glowColor = glowColor,
            elevation = elevation,
            interactionSource = interactionSource,
            onClick = onClick,
            contentColor = contentColor,
            text = text,
            isSelected = isSelected,
            glimmerBrush = glimmerBrush,
        )
    }
}

@Composable
private fun PokerChipContent(
    chipSize: Dp,
    scale: Float,
    glowAlpha: Float,
    glowColor: Color,
    elevation: Dp,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    contentColor: Color,
    text: String,
    isSelected: Boolean,
    glimmerBrush: Brush?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(chipSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.drawBehind {
                    if (glowAlpha > 0f) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                                    center = center,
                                    radius = size.minDimension * GLOW_RADIUS_FACTOR,
                                ),
                            radius = size.minDimension * GLOW_RADIUS_FACTOR,
                        )
                    }
                }.shadow(elevation, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
    ) {
        ChipFace(
            contentColor = contentColor,
            modifier = Modifier.matchParentSize(),
        )

        // Value Text on Chip
        Text(
            text = text,
            style =
                PokerTheme.typography.titleMedium.copy(
                    fontSize = if (isSelected) 20.sp else 16.sp,
                    fontWeight = FontWeight.Black,
                    shadow =
                        if (glimmerBrush == null) {
                            Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset =
                                    androidx.compose.ui.geometry
                                        .Offset(1f, 1f),
                                blurRadius = 2f,
                            )
                        } else {
                            null
                        },
                    brush = glimmerBrush,
                ),
            color = if (glimmerBrush != null) Color.White else Color.White,
        )
    }
}

@Composable
private fun rememberPokerChipAnimations(
    isSelected: Boolean,
    interactionSource: MutableInteractionSource,
): PokerChipAnimations {
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue =
            when {
                isPressed -> ELEVATION_PRESSED.dp
                isSelected -> ELEVATION_SELECTED.dp
                else -> ELEVATION_DEFAULT.dp
            },
        label = "chipElevation",
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) SCALE_SELECTED else SCALE_DEFAULT,
        label = "chipScale",
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) GLOW_ALPHA_SELECTED else 0f,
        label = "chipGlowAlpha",
    )

    return remember(elevation, scale, glowAlpha) {
        PokerChipAnimations(elevation, scale, glowAlpha)
    }
}

private data class PokerChipAnimations(
    val elevation: Dp,
    val scale: Float,
    val glowAlpha: Float,
)

@Composable
private fun ChipFace(
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // 1. Base Chip Color
        drawCircle(
            color = contentColor,
            center = center,
            radius = radius,
        )

        // 2. Dashed Ring (White spots)
        drawCircle(
            color = Color.White,
            center = center,
            radius = radius * DASH_RING_RADIUS_FACTOR,
            style =
                Stroke(
                    width = radius * DASH_RING_WIDTH_FACTOR,
                    pathEffect =
                        PathEffect.dashPathEffect(
                            intervals = floatArrayOf(DASH_INTERVAL_ON, DASH_INTERVAL_OFF),
                            phase = 0f,
                        ),
                ),
        )

        // 3. Inner Circle border
        drawCircle(
            color = Color.White.copy(alpha = INNER_BORDER_ALPHA),
            center = center,
            radius = radius * INNER_BORDER_RADIUS_FACTOR,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

private const val DASH_RING_RADIUS_FACTOR = 0.85f
private const val DASH_RING_WIDTH_FACTOR = 0.15f
private const val DASH_INTERVAL_ON = 20f
private const val DASH_INTERVAL_OFF = 20f
private const val INNER_BORDER_RADIUS_FACTOR = 0.6f
private const val INNER_BORDER_ALPHA = 0.8f

private const val SCALE_SELECTED = 1.1f
private const val SCALE_DEFAULT = 1.0f
