package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
        val comboScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (compact) 1.05f else 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Surface(
            color = if (isMegaBonus) Color(0xFFFFD700) else MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(if (compact) 6.dp else 8.dp),
            modifier = Modifier
                .scale(comboScale)
                .shadow(if (compact) 2.dp else 4.dp, RoundedCornerShape(if (compact) 6.dp else 8.dp))
        ) {
            Text(
                text = stringResource(Res.string.combo_format, combo),
                modifier = Modifier.padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 1.dp else 2.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (compact) 11.sp else 14.sp
                ),
                color = if (isMegaBonus) Color.Black else MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}
