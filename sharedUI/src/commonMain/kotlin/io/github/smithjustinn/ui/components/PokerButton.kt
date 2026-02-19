package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.PokerTheme

private const val PULSE_SCALE_TARGET = 1.05f
private const val PRESS_SCALE_TARGET = 0.95f
private const val PULSE_ANIMATION_DURATION_MS = 1000
private const val ICON_SIZE_DP = 20
private const val ICON_SPACING_DP = 8
private const val BUTTON_HEIGHT_DP = 56
private const val PRIMARY_SHADOW_ELEVATION_DP = 8
private const val PRIMARY_BORDER_WIDTH_DP = 2
private const val PRIMARY_BORDER_ALPHA = 0.5f

@Composable
fun PokerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = PokerTheme.colors.oakWood,
    contentColor: Color = PokerTheme.colors.goldenYellow,
    isPrimary: Boolean = false,
    isPulsing: Boolean = false,
    applyGlimmer: Boolean = false,
    contentDescription: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "poker_button_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing && enabled) PULSE_SCALE_TARGET else 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(PULSE_ANIMATION_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseScale",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) PRESS_SCALE_TARGET else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )

    val buttonColors = rememberPokerButtonColors(isPrimary, containerColor, contentColor, enabled)
    val shadowElevation = if (isPrimary) PRIMARY_SHADOW_ELEVATION_DP.dp else PokerTheme.spacing.extraSmall
    val border = rememberPokerButtonBorder(isPrimary)
    val hapticsService = LocalAppGraph.current.hapticsService

    Box(
        modifier =
            modifier
                .height(BUTTON_HEIGHT_DP.dp)
                .scale(pulseScale * pressScale)
                .shadow(if (enabled) shadowElevation else 0.dp, PokerTheme.shapes.medium)
                .clip(PokerTheme.shapes.medium)
                .then(if (border != null && enabled) Modifier.border(border, PokerTheme.shapes.medium) else Modifier)
                .background(buttonColors.container)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    onClick = {
                        hapticsService.performHapticFeedback(
                            if (isPrimary) HapticFeedbackType.HEAVY else HapticFeedbackType.LIGHT,
                        )
                        onClick()
                    },
                    role = Role.Button,
                ).semantics {
                    contentDescription?.let { this.contentDescription = it }
                }.padding(horizontal = PokerTheme.spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            contentColor = buttonColors.content,
            applyGlimmer = applyGlimmer && enabled,
        )
    }
}

private data class PokerButtonColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun rememberPokerButtonColors(
    isPrimary: Boolean,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
): PokerButtonColors {
    val container = if (isPrimary) PokerTheme.colors.goldenYellow else containerColor
    val content = if (isPrimary) PokerTheme.colors.feltGreenDark else contentColor

    return if (enabled) {
        PokerButtonColors(container, content)
    } else {
        PokerButtonColors(container.copy(alpha = 0.5f), content.copy(alpha = 0.5f))
    }
}

@Composable
private fun rememberPokerButtonBorder(isPrimary: Boolean): BorderStroke? =
    if (isPrimary) {
        BorderStroke(PRIMARY_BORDER_WIDTH_DP.dp, PokerTheme.colors.goldenYellow.copy(alpha = PRIMARY_BORDER_ALPHA))
    } else {
        null
    }

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    contentColor: Color,
    applyGlimmer: Boolean,
) {
    val glimmerBrush = if (applyGlimmer) rememberGlimmerBrush() else null

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(ICON_SIZE_DP.dp),
            )
            Spacer(modifier = Modifier.width(ICON_SPACING_DP.dp))
        }

        Text(
            text = text.uppercase(),
            style =
                PokerTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    brush = glimmerBrush,
                    shadow =
                        if (applyGlimmer) {
                            Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f,
                            )
                        } else {
                            null
                        },
                ),
            color = if (applyGlimmer) Color.White else contentColor, // Brush overrides color, but fallback is needed
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(ICON_SPACING_DP.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(ICON_SIZE_DP.dp),
            )
        }
    }
}
