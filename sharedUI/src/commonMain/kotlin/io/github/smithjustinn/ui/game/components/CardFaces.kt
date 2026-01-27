package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit

@Composable
internal fun CardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    theme: CardSymbolTheme,
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        // Base scaling on the card width (maxWidth is a Dp value)
        val baseSize = maxWidth
        val fontScale = density.fontScale

        // Helper to get size that ignores system font scaling
        fun getFontSize(size: Float) = (size * (baseSize.value / BASE_CARD_WIDTH) / fontScale).sp

        when (theme) {
            CardSymbolTheme.CLASSIC -> ClassicCardFace(rank, suit, suitColor, ::getFontSize)
            CardSymbolTheme.MINIMAL -> MinimalCardFace(rank, suit, suitColor, ::getFontSize)
            CardSymbolTheme.TEXT_ONLY -> TextOnlyCardFace(rank, suit, suitColor, ::getFontSize)
            CardSymbolTheme.POKER -> PokerCardFace(rank, suit, suitColor, ::getFontSize)
        }
    }
}

@Composable
private fun ClassicCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
) {
    // Top Left
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = rank.symbol,
            color = suitColor,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = getFontSize(FONT_SIZE_MEDIUM),
                ),
        )
        Text(
            text = suit.symbol,
            color = suitColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = getFontSize(FONT_SIZE_SMALL)),
        )
    }

    // Center Suit
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = VERY_LOW_ALPHA),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = getFontSize(FONT_SIZE_HUGE)),
        )

        Text(
            text = rank.symbol,
            color = suitColor,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = getFontSize(FONT_SIZE_TITLE),
                ),
        )
    }

    // Bottom Right
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { rotationZ = FULL_ROTATION },
        ) {
            Text(
                text = rank.symbol,
                color = suitColor,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = getFontSize(FONT_SIZE_MEDIUM),
                    ),
            )
            Text(
                text = suit.symbol,
                color = suitColor,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = getFontSize(FONT_SIZE_SMALL)),
            )
        }
    }
}

@Composable
private fun MinimalCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Large Rank in Center
        Text(
            text = rank.symbol,
            color = suitColor,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = getFontSize(FONT_SIZE_DISPLAY),
                ),
            modifier = Modifier.align(Alignment.Center),
        )

        // Small Suit in Corners
        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = MODERATE_ALPHA),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = getFontSize(FONT_SIZE_LARGE)),
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
        )

        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = MODERATE_ALPHA),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = getFontSize(FONT_SIZE_LARGE)),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .graphicsLayer { rotationZ = FULL_ROTATION },
        )
    }
}

@Composable
private fun TextOnlyCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Just the rank, no suit symbols
        Text(
            text = rank.symbol,
            color = suitColor,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = getFontSize(FONT_SIZE_HERO),
                ),
            modifier = Modifier.align(Alignment.Center),
        )

        // Suit name instead of symbol at bottom
        Text(
            text = suit.name.lowercase().replaceFirstChar { it.uppercase() },
            color = suitColor.copy(alpha = HALF_ALPHA),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = getFontSize(FONT_SIZE_SMALL),
                    fontWeight = FontWeight.Medium,
                ),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PokerCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
) {
    // Similar to Classic but using Serif font and slightly different layout
    val serifTypography = MaterialTheme.typography.labelLarge.copy(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
    )
    
    // Top Left
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = rank.symbol,
            color = suitColor,
            style = serifTypography.copy(
                fontWeight = FontWeight.Bold,
                fontSize = getFontSize(FONT_SIZE_MEDIUM),
            ),
        )
        Text(
            text = suit.symbol,
            color = suitColor,
            style = serifTypography.copy(
                fontSize = getFontSize(FONT_SIZE_SMALL),
                fontWeight = FontWeight.Normal
            ),
        )
    }

    // Center Suit
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = 0.1f), // Very subtle watermark
            style = serifTypography.copy(fontSize = getFontSize(FONT_SIZE_HUGE)),
        )

        Text(
            text = rank.symbol,
            color = suitColor,
            style = serifTypography.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = getFontSize(FONT_SIZE_TITLE),
            ),
        )
    }

    // Bottom Right (Rotated)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { rotationZ = FULL_ROTATION },
        ) {
            Text(
                text = rank.symbol,
                color = suitColor,
                style = serifTypography.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = getFontSize(FONT_SIZE_MEDIUM),
                ),
            )
            Text(
                text = suit.symbol,
                color = suitColor,
                style = serifTypography.copy(
                    fontSize = getFontSize(FONT_SIZE_SMALL),
                    fontWeight = FontWeight.Normal
                ),
            )
        }
    }
}
