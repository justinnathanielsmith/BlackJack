package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme

private const val PULSE_SCALE_TARGET = 1.05f
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
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = PokerTheme.colors.oakWood,
    contentColor: Color = PokerTheme.colors.goldenYellow,
    isPrimary: Boolean = false,
    isPulsing: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "poker_button_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) PULSE_SCALE_TARGET else 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(PULSE_ANIMATION_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "scale",
    )

    val finalContainerColor = if (isPrimary) PokerTheme.colors.goldenYellow else containerColor
    val finalContentColor = if (isPrimary) PokerTheme.colors.feltGreenDark else contentColor
    val shadowElevation = if (isPrimary) PRIMARY_SHADOW_ELEVATION_DP.dp else PokerTheme.spacing.extraSmall
    val border =
        if (isPrimary) {
            BorderStroke(PRIMARY_BORDER_WIDTH_DP.dp, PokerTheme.colors.goldenYellow.copy(alpha = PRIMARY_BORDER_ALPHA))
        } else {
            null
        }

    Box(
        modifier =
            modifier
                .height(BUTTON_HEIGHT_DP.dp)
                .scale(scale)
                .shadow(shadowElevation, PokerTheme.shapes.medium)
                .clip(PokerTheme.shapes.medium)
                .then(if (border != null) Modifier.border(border, PokerTheme.shapes.medium) else Modifier)
                .background(finalContainerColor)
                .clickable(onClick = onClick)
                .padding(horizontal = PokerTheme.spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            contentColor = finalContentColor,
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    contentColor: Color,
) {
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
            style = PokerTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
