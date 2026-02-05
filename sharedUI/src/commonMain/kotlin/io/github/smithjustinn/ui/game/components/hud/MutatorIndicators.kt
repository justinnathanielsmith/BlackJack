package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.mutator_blackout
import io.github.smithjustinn.resources.mutator_mirage
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MutatorIndicators(
    activeMutators: Set<DailyChallengeMutator>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        activeMutators.forEach { mutator ->
            MutatorBadge(mutator = mutator, compact = compact)
        }
    }
}

@Composable
private fun MutatorBadge(
    mutator: DailyChallengeMutator,
    compact: Boolean,
) {
    val config = getMutatorConfig(mutator)
    val alpha by rememberPulsingAlpha()

    Row(
        modifier =
            Modifier
                .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    brush =
                        Brush.linearGradient(
                            listOf(config.color.copy(alpha = 0.8f), config.color.copy(alpha = 0.2f)),
                        ),
                    shape = RoundedCornerShape(12.dp),
                ).padding(
                    horizontal = if (compact) 8.dp else 12.dp,
                    vertical = if (compact) 4.dp else 6.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.color.copy(alpha = alpha),
            modifier = Modifier.size(if (compact) 16.dp else 20.dp),
        )

        Text(
            text = stringResource(config.labelRes),
            style =
                PokerTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
            color = Color.White.copy(alpha = alpha),
        )
    }
}

@Composable
private fun rememberPulsingAlpha() =
    rememberInfiniteTransition(label = "mutator_glimmer").animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "alpha",
    )

private data class MutatorConfig(
    val icon: ImageVector,
    val labelRes: StringResource,
    val color: Color,
)

@Composable
private fun getMutatorConfig(mutator: DailyChallengeMutator): MutatorConfig =
    when (mutator) {
        DailyChallengeMutator.BLACKOUT ->
            MutatorConfig(
                icon = AppIcons.VisibilityOff,
                labelRes = Res.string.mutator_blackout,
                color = PokerTheme.colors.tacticalRed,
            )
        DailyChallengeMutator.MIRAGE ->
            MutatorConfig(
                icon = AppIcons.SwapHoriz,
                labelRes = Res.string.mutator_mirage,
                color = PokerTheme.colors.softBlue,
            )
    }
