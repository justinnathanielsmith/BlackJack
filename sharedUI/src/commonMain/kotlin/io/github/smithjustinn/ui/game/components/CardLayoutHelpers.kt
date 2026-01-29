package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.PokerTheme
import kotlin.math.abs

private const val SHADOW_AMBIENT_ALPHA = 0.2f
private const val SHADOW_SPOT_ALPHA = 0.4f
private const val CORNER_RADIUS_DP = 12
private const val RIM_LIGHT_THRESHOLD = 0.1f
private const val BORDER_SIZE_MULTIPLIER = 2f

@Composable
fun CardShadowLayer(
    elevation: Dp,
    yOffset: Dp,
    isRecentlyMatched: Boolean,
) {
    if (elevation <= 0.dp) return

    val glowColor = PokerTheme.colors.goldenYellow
    val baseShadowColor = if (isRecentlyMatched) glowColor else Color.Black

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = yOffset.toPx()
                }.shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(CORNER_RADIUS_DP.dp),
                    clip = false,
                    ambientColor = baseShadowColor.copy(alpha = SHADOW_AMBIENT_ALPHA),
                    spotColor = baseShadowColor.copy(alpha = SHADOW_SPOT_ALPHA),
                ),
    )
}

@Composable
fun getCardBorder(
    rotation: Float,
    visualState: CardVisualState,
): BorderStroke =
    if (rotation <= HALF_ROTATION) {
        when {
            visualState.isRecentlyMatched -> BorderStroke(2.dp, PokerTheme.colors.goldenYellow)
            visualState.isMatched -> BorderStroke(1.dp, PokerTheme.colors.goldenYellow.copy(alpha = MEDIUM_ALPHA))
            visualState.isError -> BorderStroke(3.dp, MaterialTheme.colorScheme.error)
            else -> BorderStroke(1.dp, Color.LightGray.copy(alpha = HALF_ALPHA))
        }
    } else {
        val rimLightAlpha = (1f - (abs(rotation - HALF_ROTATION) / HALF_ROTATION)).coerceIn(0f, 1f)
        val rimLightColor = Color.White.copy(alpha = rimLightAlpha * HIGH_ALPHA)
        BorderStroke(
            width = (2.dp + (rimLightAlpha * BORDER_SIZE_MULTIPLIER).dp),
            color = if (rimLightAlpha > RIM_LIGHT_THRESHOLD) rimLightColor else Color.White.copy(alpha = SUBTLE_ALPHA),
        )
    }

@Composable
fun calculateSuitColor(
    suit: Suit,
    areSuitsMultiColored: Boolean,
    theme: CardSymbolTheme,
): Color =
    if (theme == CardSymbolTheme.POKER) {
        if (suit.isRed) PokerTheme.colors.tacticalRed else Color.Black
    } else if (areSuitsMultiColored) {
        when (suit) {
            Suit.Hearts -> PokerTheme.colors.tacticalRed
            Suit.Diamonds -> PokerTheme.colors.softBlue
            Suit.Clubs -> PokerTheme.colors.bonusGreen
            Suit.Spades -> Color.Black
        }
    } else {
        if (suit.isRed) PokerTheme.colors.tacticalRed else Color.Black
    }
