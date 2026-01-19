package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.combo_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun ComboBadge(
    combo: Int,
    isMegaBonus: Boolean,
    infiniteTransition: InfiniteTransition,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    AnimatedVisibility(
        visible = combo > 1,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val comboPulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (compact) 1.05f else 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val badgeColor = if (isMegaBonus) Color(0xFFFFD700) else NeonCyan
        val tacticalShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

        Surface(
            color = InactiveBackground.copy(alpha = 0.6f),
            shape = tacticalShape,
            border = BorderStroke(1.5.dp, badgeColor),
            modifier = Modifier
                .scale(comboPulseScale)
                .shadow(
                    elevation = if (compact) 6.dp else 12.dp,
                    shape = tacticalShape,
                    ambientColor = badgeColor,
                    spotColor = badgeColor
                )
        ) {
            Text(
                text = stringResource(Res.string.combo_format, combo).uppercase(),
                modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 2.dp else 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (compact) 12.sp else 16.sp,
                    letterSpacing = 1.sp
                ),
                color = badgeColor
            )
        }
    }
}
