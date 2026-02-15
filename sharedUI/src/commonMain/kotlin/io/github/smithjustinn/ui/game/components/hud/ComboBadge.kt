package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.combo_badge_format
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

private const val COMBO_ANIMATION_DURATION_MS = 400
private const val PULSE_SCALE_HEAT_COMPACT = 1.08f
private const val PULSE_SCALE_HEAT = 1.15f
private const val PULSE_SCALE_DEFAULT_COMPACT = 1.05f
private const val PULSE_SCALE_DEFAULT = 1.1f

private const val CHIP_SIZE_COMPACT = 32
private const val CHIP_SIZE_NORMAL = 48
private const val STACK_OFFSET_COMPACT = 2
private const val STACK_OFFSET_NORMAL = 4
private const val MAX_CHIPS_IN_STACK = 5
private const val CHIP_RING_ALPHA = 0.9f
private const val CHIP_RING_RADIUS_FACTOR = 0.85f
private const val CHIP_RING_WIDTH_FACTOR = 0.15f
private const val CHIP_INNER_ALPHA = 0.4f
private const val CHIP_INNER_RADIUS_FACTOR = 0.6f
private const val CHIP_SHADOW_ALPHA = 0.5f
private const val CHIP_DASH_ON = 20f
private const val CHIP_DASH_OFF = 20f
private const val CHIP_FONT_SIZE_COMPACT = 10
private const val CHIP_FONT_SIZE_NORMAL = 14

@Composable
fun ComboBadge(
    state: ComboBadgeState,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    AnimatedVisibility(
        visible = state.combo > 1,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        val comboPulseScaleState =
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue =
                    when {
                        state.isHeatMode -> if (compact) PULSE_SCALE_HEAT_COMPACT else PULSE_SCALE_HEAT
                        else -> if (compact) PULSE_SCALE_DEFAULT_COMPACT else PULSE_SCALE_DEFAULT
                    },
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(COMBO_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "comboPulse",
            )

        ComboBadgeContent(
            state = state,
            compact = compact,
            pulseScale = comboPulseScaleState,
        )
    }
}

@Composable
@Suppress("ktlint:compose:state-param-check")
private fun ComboBadgeContent(
    state: ComboBadgeState,
    compact: Boolean,
    pulseScale: State<Float>,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors
    val badgeColor =
        when {
            state.isHeatMode -> colors.tacticalRed
            state.isMegaBonus -> colors.goldenYellow
            else -> colors.goldenYellow
        }

    val chipSize = if (compact) CHIP_SIZE_COMPACT.dp else CHIP_SIZE_NORMAL.dp
    val stackOffset = if (compact) STACK_OFFSET_COMPACT.dp else STACK_OFFSET_NORMAL.dp

    // Show at most 5 chips in the stack, but at least 1 for the combo itself
    val chipCount = (state.combo - 1).coerceIn(1, MAX_CHIPS_IN_STACK)

    Box(
        modifier =
            modifier.graphicsLayer {
                // Bolt: Defer state read to graphicsLayer to skip recomposition on every animation frame
                val scale = pulseScale.value
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Render the stack of chips
        repeat(chipCount) { index ->
            val yOffset = -stackOffset * index
            ChipVisual(
                color = badgeColor,
                chipSize = chipSize,
                modifier =
                    Modifier
                        .padding(bottom = yOffset.coerceAtLeast(0.dp))
                        .shadow(
                            elevation = (index + 1).dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = CHIP_SHADOW_ALPHA),
                            spotColor = Color.Black.copy(alpha = CHIP_SHADOW_ALPHA),
                        ),
            )
        }

        // Top Chip with Text
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = (stackOffset * (chipCount - 1)).coerceAtLeast(0.dp)),
        ) {
            ChipVisual(
                color = badgeColor,
                chipSize = chipSize,
                modifier =
                    Modifier.shadow(
                        elevation = (chipCount + 1).dp,
                        shape = CircleShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black,
                    ),
            )

            Text(
                text = stringResource(Res.string.combo_badge_format, state.combo),
                style =
                    PokerTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (compact) CHIP_FONT_SIZE_COMPACT.sp else CHIP_FONT_SIZE_NORMAL.sp,
                    ),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ChipVisual(
    color: Color,
    chipSize: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(chipSize)) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // 1. Base Chip Color
        drawCircle(
            color = color,
            center = center,
            radius = radius,
        )

        // 2. Dashed Ring (White spots)
        drawCircle(
            color = Color.White.copy(alpha = CHIP_RING_ALPHA),
            center = center,
            radius = radius * CHIP_RING_RADIUS_FACTOR,
            style =
                Stroke(
                    width = radius * CHIP_RING_WIDTH_FACTOR,
                    pathEffect =
                        PathEffect.dashPathEffect(
                            intervals = floatArrayOf(CHIP_DASH_ON, CHIP_DASH_OFF),
                            phase = 0f,
                        ),
                ),
        )

        // 3. Inner Circle border
        drawCircle(
            color = Color.White.copy(alpha = CHIP_INNER_ALPHA),
            center = center,
            radius = radius * CHIP_INNER_RADIUS_FACTOR,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
