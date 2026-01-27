package io.github.smithjustinn.ui.game.components

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.combo_format
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

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
        val comboPulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue =
                if (state.isHeatMode) {
                    if (compact) 1.08f else 1.15f
                } else {
                    if (compact) 1.05f else 1.1f
                },
            animationSpec =
                infiniteRepeatable(
                    animation = tween(COMBO_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

        ComboBadgeContent(
            state = state,
            compact = compact,
            pulseScale = comboPulseScale,
        )
    }
}

@Composable
private fun ComboBadgeContent(
    state: ComboBadgeState,
    compact: Boolean,
    pulseScale: Float,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing
    val badgeColor =
        when {
            state.isHeatMode -> colors.tacticalRed
            state.isMegaBonus -> colors.goldenYellow
            else -> colors.goldenYellow // Gold default
        }
    // Chip Shape (Circle or Rounded)
    val chipShape = androidx.compose.foundation.shape.RoundedCornerShape(50)

    Surface(
        color = colors.oakWood,
        shape = chipShape,
        border = BorderStroke(BORDER_WIDTH.dp, badgeColor),
        modifier =
            modifier
                .scale(pulseScale)
                .shadow(
                    elevation =
                        if (state.isHeatMode) {
                            if (compact) spacing.small else spacing.medium
                        } else {
                            if (compact) spacing.extraSmall else spacing.small
                        },
                    shape = chipShape,
                    ambientColor = Color.Black, // Dark shadow
                    spotColor = Color.Black,
                    clip = false,
                ),
    ) {
        Text(
            text = stringResource(Res.string.combo_format, state.combo).uppercase(),
            modifier =
                Modifier.padding(
                    horizontal = if (compact) spacing.small else spacing.medium,
                    vertical = if (compact) spacing.extraSmall else spacing.small,
                ),
            style =
                PokerTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 12.sp else 16.sp,
                    letterSpacing = 1.sp,
                ),
            color = badgeColor,
        )
    }
}

private const val COMBO_ANIMATION_DURATION_MS = 400
private const val BORDER_WIDTH = 1.5
