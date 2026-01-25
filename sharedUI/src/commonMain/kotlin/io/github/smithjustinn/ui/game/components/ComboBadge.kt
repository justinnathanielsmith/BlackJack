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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.HeatGlowPrimary
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.combo_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun ComboBadge(
    combo: Int,
    isMegaBonus: Boolean,
    isHeatMode: Boolean,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    AnimatedVisibility(
        visible = combo > 1,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        val comboPulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isHeatMode) {
                if (compact) 1.08f else 1.15f
            } else {
                if (compact) 1.05f else 1.1f
            },
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )

        val badgeColor = when {
            isHeatMode -> HeatGlowPrimary
            isMegaBonus -> GoldenYellow
            else -> NeonCyan
        }
        val tacticalShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

        Surface(
            color = InactiveBackground.copy(alpha = 0.6f),
            shape = tacticalShape,
            border = BorderStroke(1.5.dp, badgeColor),
            modifier = Modifier
                .scale(comboPulseScale)
                .shadow(
                    elevation = if (isHeatMode) {
                        if (compact) 10.dp else 18.dp
                    } else {
                        if (compact) 6.dp else 12.dp
                    },
                    shape = tacticalShape,
                    ambientColor = badgeColor,
                    spotColor = badgeColor,
                ),
        ) {
            Text(
                text = stringResource(Res.string.combo_format, combo).uppercase(),
                modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 2.dp else 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (compact) 12.sp else 16.sp,
                    letterSpacing = 1.sp,
                ),
                color = badgeColor,
            )
        }
    }
}
