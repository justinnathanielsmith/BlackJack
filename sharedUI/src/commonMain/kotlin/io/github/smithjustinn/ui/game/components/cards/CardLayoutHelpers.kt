package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.PokerTheme
import kotlin.math.abs

private const val SHADOW_AMBIENT_ALPHA = 0.3f
private const val SHADOW_SPOT_ALPHA = 0.6f
private const val CORNER_RADIUS_DP = 12
private const val RIM_LIGHT_THRESHOLD = 0.1f
private const val BORDER_SIZE_MULTIPLIER = 2f

@Composable
@Suppress("ktlint:compose:state-param-check")
fun CardShadowLayer(
    elevation: State<Dp>,
    yOffset: State<Dp>,
    isRecentlyMatched: Boolean,
) {
    val glowColor = PokerTheme.colors.goldenYellow
    val baseShadowColor = if (isRecentlyMatched) glowColor else PokerTheme.colors.tableShadow
    val shadowShape = remember { RoundedCornerShape(CORNER_RADIUS_DP.dp) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = yOffset.value.toPx()
                    shadowElevation = elevation.value.toPx()
                    shape = shadowShape
                    clip = false
                    ambientShadowColor = baseShadowColor.copy(alpha = SHADOW_AMBIENT_ALPHA)
                    spotShadowColor = baseShadowColor.copy(alpha = SHADOW_SPOT_ALPHA)
                },
    )
}

@Composable
@Suppress("ktlint:compose:state-param-check")
fun Modifier.cardBorder(
    rotation: State<Float>,
    visualState: CardVisualState,
): Modifier {
    val primaryColor = PokerTheme.colors.goldenYellow
    val errorColor = MaterialTheme.colorScheme.error
    val lightGray = Color.LightGray

    return this.drawWithContent {
        drawContent()

        val currentRotation = rotation.value

        if (currentRotation <= HALF_ROTATION) {
            val (width, color) =
                when {
                    visualState.isRecentlyMatched -> 2.dp to primaryColor
                    visualState.isMatched -> 1.dp to primaryColor.copy(alpha = MEDIUM_ALPHA)
                    visualState.isError -> 3.dp to errorColor
                    else -> 1.dp to lightGray.copy(alpha = HALF_ALPHA)
                }

            drawRoundRect(
                color = color,
                size = size,
                cornerRadius =
                    androidx.compose.ui.geometry
                        .CornerRadius(CORNER_RADIUS_DP.dp.toPx()),
                style = Stroke(width = width.toPx()),
            )
        } else {
            val rimLightAlpha = (1f - abs(currentRotation - HALF_ROTATION) / HALF_ROTATION).coerceIn(0f, 1f)
            val rimLightColor = Color.White.copy(alpha = rimLightAlpha * HIGH_ALPHA)

            val width = 2.dp + (rimLightAlpha * BORDER_SIZE_MULTIPLIER).dp
            val color =
                if (rimLightAlpha >
                    RIM_LIGHT_THRESHOLD
                ) {
                    rimLightColor
                } else {
                    Color.White.copy(alpha = SUBTLE_ALPHA)
                }

            drawRoundRect(
                color = color,
                size = size,
                cornerRadius =
                    androidx.compose.ui.geometry
                        .CornerRadius(CORNER_RADIUS_DP.dp.toPx()),
                style = Stroke(width = width.toPx()),
            )
        }
    }
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
