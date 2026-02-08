package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.PokerTheme

object CardFaces {
    @Composable
    fun Classic(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            ClassicCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Minimal(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            MinimalCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun TextOnly(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            TextOnlyCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Poker(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            PokerCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Neon(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            NeonCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Retro(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            RetroCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Elegant(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            ElegantCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Cyberpunk(modifier: Modifier = Modifier) =
        PreviewContainer(modifier) {
            CyberpunkCardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = Color.Black,
                getFontSize = { getPreviewFontSize(it) },
            )
        }

    @Composable
    fun Render(
        theme: CardTheme,
        rank: Rank,
        suit: Suit,
        suitColor: Color,
        modifier: Modifier = Modifier,
    ) {
        Box(modifier = modifier) {
            CardFace(
                rank = rank,
                suit = suit,
                suitColor = suitColor,
                theme = theme.skin,
            )
        }
    }

    @Composable
    private fun PreviewContainer(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        Card(
            modifier = modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                content()
            }
        }
    }

    private fun getPreviewFontSize(baseSize: Float) = (baseSize * (80f / BASE_CARD_WIDTH)).sp
}

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
            CardSymbolTheme.NEON -> NeonCardFace(rank, suit, ::getFontSize)
            CardSymbolTheme.RETRO -> RetroCardFace(rank, suit, suitColor, ::getFontSize)
            CardSymbolTheme.ELEGANT -> ElegantCardFace(rank, suit, suitColor, ::getFontSize)
            CardSymbolTheme.CYBERPUNK -> CyberpunkCardFace(rank, suit, suitColor, ::getFontSize)
        }
    }
}

@Composable
internal fun NeonCardFace(
    rank: Rank,
    suit: Suit,
    getFontSize: (Float) -> TextUnit,
) {
    val neonColor = if (suit.isRed) Color(0xFFFF0055) else Color(0xFF00FFFF)
    val shadow =
        Shadow(
            color = neonColor,
            blurRadius = 8f,
        )

    Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        // Center Rank
        Text(
            text = rank.symbol,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = getFontSize(FONT_SIZE_HERO),
                    shadow = shadow,
                    fontFamily = FontFamily.SansSerif,
                ),
            color = neonColor,
            modifier = Modifier.align(Alignment.Center),
        )

        // Corners
        NeonCardCorner(
            rank = rank,
            suit = suit,
            neonColor = neonColor,
            shadow = shadow,
            getFontSize = getFontSize,
            modifier = Modifier.align(Alignment.TopStart),
        )

        NeonCardCorner(
            rank = rank,
            suit = suit,
            neonColor = neonColor,
            shadow = shadow,
            getFontSize = getFontSize,
            modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { rotationZ = FULL_ROTATION },
        )
    }
}

@Composable
private fun NeonCardCorner(
    rank: Rank,
    suit: Suit,
    neonColor: Color,
    shadow: Shadow,
    getFontSize: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = rank.symbol,
            color = neonColor,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = getFontSize(FONT_SIZE_MEDIUM),
                    shadow = shadow,
                ),
        )
        Text(
            text = suit.symbol,
            color = neonColor,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = getFontSize(FONT_SIZE_SMALL),
                    shadow = shadow,
                ),
        )
    }
}

@Composable
internal fun RetroCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
) {
    val monoStyle =
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )

    Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        // Corners like a command prompt
        Text(
            text = "${rank.symbol} ${suit.symbol}",
            color = suitColor,
            style = monoStyle.copy(fontSize = getFontSize(FONT_SIZE_MEDIUM)),
            modifier = Modifier.align(Alignment.TopStart),
        )

        Text(
            text = "${rank.symbol} ${suit.symbol}",
            color = suitColor,
            style = monoStyle.copy(fontSize = getFontSize(FONT_SIZE_MEDIUM)),
            modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { rotationZ = FULL_ROTATION },
        )

        // Center pixel art ish
        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = 0.2f),
            style = monoStyle.copy(fontSize = getFontSize(FONT_SIZE_HUGE)),
            modifier = Modifier.align(Alignment.Center),
        )
        Text(
            text = rank.symbol,
            color = suitColor,
            style = monoStyle.copy(fontSize = getFontSize(FONT_SIZE_DISPLAY)),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
internal fun ElegantCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
) {
    val elegantStyle =
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
        )

    // Decorative border
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 2.dp.toPx()
        drawRect(
            color = suitColor.copy(alpha = 0.3f),
            topLeft = Offset(strokeWidth * 3, strokeWidth * 3),
            size =
                size.copy(
                    width = size.width - strokeWidth * 6,
                    height = size.height - strokeWidth * 6,
                ),
            style = Stroke(width = strokeWidth),
        )
    }

    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Centered Rank only, very stylish
        Text(
            text = rank.symbol,
            color = suitColor,
            style = elegantStyle.copy(fontSize = getFontSize(FONT_SIZE_HERO)),
            modifier = Modifier.align(Alignment.Center),
        )

        // Suit at top center and bottom center
        Text(
            text = suit.symbol,
            color = suitColor,
            style = elegantStyle.copy(fontSize = getFontSize(FONT_SIZE_LARGE)),
            modifier = Modifier.align(Alignment.TopCenter),
        )
        Text(
            text = suit.symbol,
            color = suitColor,
            style = elegantStyle.copy(fontSize = getFontSize(FONT_SIZE_LARGE)),
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { rotationZ = FULL_ROTATION },
        )
    }
}

@Composable
internal fun CyberpunkCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
) {
    val glitchColor = if (suitColor == Color.Black) Color.DarkGray else Color.Magenta

    Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        // Glitchy Center Rank
        // Offset shadow
        Text(
            text = rank.symbol,
            color = glitchColor.copy(alpha = 0.5f),
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = getFontSize(FONT_SIZE_DISPLAY),
                    fontFamily = FontFamily.Monospace,
                ),
            modifier = Modifier.align(Alignment.Center).offset(x = 2.dp, y = 2.dp),
        )
        Text(
            text = rank.symbol,
            color = suitColor,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = getFontSize(FONT_SIZE_DISPLAY),
                    fontFamily = FontFamily.Monospace,
                ),
            modifier = Modifier.align(Alignment.Center).offset(x = (-2).dp, y = (-2).dp),
        )

        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = rank.symbol,
                color = suitColor,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = getFontSize(FONT_SIZE_MEDIUM),
                        fontFamily = FontFamily.Monospace,
                    ),
            )
        }

        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
            Text(
                text = suit.symbol,
                color = suitColor,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = getFontSize(FONT_SIZE_MEDIUM),
                        fontFamily = FontFamily.Monospace,
                    ),
            )
        }
    }
}

@Composable
internal fun ClassicCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
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
internal fun MinimalCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
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
internal fun TextOnlyCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
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
internal fun PokerCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> TextUnit,
) {
    PokerCardBorder()

    // Serif Typography for Premium Look
    val serifTypography =
        MaterialTheme.typography.displaySmall.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
        )

    val labelTypography =
        MaterialTheme.typography.labelMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
        )

    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Top Left Jumbo Index
        PokerCardCornerIndex(
            rank = rank,
            suit = suit,
            suitColor = suitColor,
            serifStyle = serifTypography,
            labelStyle = labelTypography,
            getFontSize = getFontSize,
            modifier = Modifier.align(Alignment.TopStart),
        )

        // Center Elegant Element
        PokerCardCenterContent(
            rank = rank,
            suit = suit,
            suitColor = suitColor,
            serifStyle = serifTypography,
            getFontSize = getFontSize,
            modifier = Modifier.align(Alignment.Center),
        )

        // Bottom Right Jumbo Index (Inverted)
        PokerCardCornerIndex(
            rank = rank,
            suit = suit,
            suitColor = suitColor,
            serifStyle = serifTypography,
            labelStyle = labelTypography,
            getFontSize = getFontSize,
            modifier = Modifier.align(Alignment.BottomEnd).graphicsLayer { rotationZ = FULL_ROTATION },
        )
    }
}

@Composable
private fun PokerCardBorder() {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 2.dp.toPx() }
    val borderColor = PokerTheme.colors.goldenYellow

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = borderColor,
            style = Stroke(width = strokeWidth),
        )
        // Inner thin border
        drawRect(
            color = borderColor.copy(alpha = 0.5f),
            topLeft = Offset(strokeWidth * 2, strokeWidth * 2),
            size = size.copy(width = size.width - strokeWidth * 4, height = size.height - strokeWidth * 4),
            style = Stroke(width = strokeWidth / 2),
        )
    }
}

@Composable
private fun PokerCardCornerIndex(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    serifStyle: TextStyle,
    labelStyle: TextStyle,
    getFontSize: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = rank.symbol,
            color = suitColor,
            style = serifStyle.copy(fontSize = getFontSize(FONT_SIZE_TITLE)),
            lineHeight = getFontSize(FONT_SIZE_TITLE),
        )
        Text(
            text = suit.symbol,
            color = suitColor,
            style = labelStyle.copy(fontSize = getFontSize(FONT_SIZE_MEDIUM)),
            modifier = Modifier.align(Alignment.CenterHorizontally).offset(y = (-4).dp),
        )
    }
}

@Composable
private fun PokerCardCenterContent(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    serifStyle: TextStyle,
    getFontSize: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Text(
            text = suit.symbol,
            color = suitColor.copy(alpha = 0.15f),
            style = serifStyle.copy(fontSize = getFontSize(FONT_SIZE_HUGE)),
        )
        Text(
            text = rank.symbol,
            color = suitColor.copy(alpha = 0.8f),
            style =
                serifStyle.copy(
                    fontSize = getFontSize(FONT_SIZE_DISPLAY),
                    shadow =
                        Shadow(
                            color =
                                PokerTheme.colors.goldenYellow
                                    .copy(alpha = 0.5f),
                            blurRadius = 4f,
                        ),
                ),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
