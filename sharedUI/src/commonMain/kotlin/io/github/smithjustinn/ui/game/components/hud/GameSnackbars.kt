package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.new_high_score
import io.github.smithjustinn.theme.ModernGold
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewHighScoreSnackbar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "HighScorePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "Scale",
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = PokerTheme.colors.oakWood,
                contentColor = Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier =
            modifier
                .scale(scale)
                .border(
                    width = 2.dp,
                    brush =
                        Brush.linearGradient(
                            listOf(ModernGold, ModernGold.copy(alpha = 0.5f), ModernGold),
                        ),
                    shape = RoundedCornerShape(12.dp),
                ),
    ) {
        NewHighScoreContent()
    }
}

@Composable
private fun NewHighScoreContent() {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = AppIcons.Trophy,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = ModernGold,
        )
        Text(
            text = stringResource(Res.string.new_high_score).uppercase(),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                ),
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
        Icon(
            imageVector = AppIcons.Trophy,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = ModernGold,
        )
    }
}
