package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
        androidx.compose.material3.Card(
            modifier = modifier.fillMaxSize(),
            shape =
                androidx.compose.foundation.shape
                    .RoundedCornerShape(12.dp),
            colors =
                androidx.compose.material3.CardDefaults.cardColors(
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
        }
    }
}

@Composable
internal fun ClassicCardFace(
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
internal fun MinimalCardFace(
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
internal fun TextOnlyCardFace(
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
internal fun PokerCardFace(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
) {
    PokerCardBorder()

    // Serif Typography for Premium Look
    val serifTypography =
        MaterialTheme.typography.displaySmall.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            fontWeight = FontWeight.Bold,
        )

    val labelTypography =
        MaterialTheme.typography.labelMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
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

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = borderColor,
            style =
                androidx.compose.ui.graphics.drawscope
                    .Stroke(width = strokeWidth),
        )
        // Inner thin border
        drawRect(
            color = borderColor.copy(alpha = 0.5f),
            topLeft =
                androidx.compose.ui.geometry
                    .Offset(strokeWidth * 2, strokeWidth * 2),
            size = size.copy(width = size.width - strokeWidth * 4, height = size.height - strokeWidth * 4),
            style =
                androidx.compose.ui.graphics.drawscope
                    .Stroke(width = strokeWidth / 2),
        )
    }
}

@Composable
private fun PokerCardCornerIndex(
    rank: Rank,
    suit: Suit,
    suitColor: Color,
    serifStyle: androidx.compose.ui.text.TextStyle,
    labelStyle: androidx.compose.ui.text.TextStyle,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
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
    serifStyle: androidx.compose.ui.text.TextStyle,
    getFontSize: (Float) -> androidx.compose.ui.unit.TextUnit,
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
                        androidx.compose.ui.graphics.Shadow(
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
