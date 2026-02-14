package io.github.smithjustinn.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.game.components.cards.CardBack
import io.github.smithjustinn.ui.game.components.cards.CardFace
import io.github.smithjustinn.ui.game.components.cards.ShimmerEffect
import io.github.smithjustinn.ui.game.components.grid.CARD_ASPECT_RATIO
import io.github.smithjustinn.utils.Constants

/**
 * AssetProvider provides Composable previews for shop items.
 * Maps shopItemId strings to their visual representations.
 */
object AssetProvider {

    /**
     * Resolves a CardBackTheme from a shop item ID.
     */
    fun getBackTheme(id: String): CardBackTheme? =
        CardBackTheme.entries.find { id.startsWith(it.id) } ?: CardBackTheme.entries.firstOrNull { it.id == id }

    /**
     * Resolves a CardSymbolTheme from a shop item ID.
     */
    fun getSymbolTheme(id: String): CardSymbolTheme? = CardSymbolTheme.entries.firstOrNull { it.id == id }

    /**
     * Renders a preview of a card asset based on the shop item ID.
     * - For card back themes: Shows the back pattern
     * - For card skins: Shows an Ace of Spades in that style
     */
    @Composable
    fun CardPreview(
        shopItemId: String,
        modifier: Modifier = Modifier,
        hexColor: String? = null,
    ) {
        val backTheme = getBackTheme(shopItemId)
        val symbolTheme = getSymbolTheme(shopItemId)

        Box(
            modifier =
                modifier
                    .aspectRatio(CARD_ASPECT_RATIO)
                    .padding(PokerTheme.spacing.small)
                    .clip(PokerTheme.shapes.medium)
                    .background(Color.White),
        ) {
            when {
                backTheme != null -> {
                    // Render card back preview
                    CardBackPreview(
                        theme = backTheme,
                        hexColor = hexColor,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                symbolTheme != null -> {
                    // Render card face preview (Ace of Spades)
                    CardFacePreview(
                        theme = symbolTheme,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                shopItemId == Constants.FEATURE_FOUR_COLOR_SUITS -> {
                    FourColorPreview(modifier = Modifier.fillMaxSize())
                }
                shopItemId == Constants.FEATURE_THIRD_EYE -> {
                    ThirdEyePreview(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    // Unknown ID - render empty box
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    @Composable
    private fun CardBackPreview(
        theme: CardBackTheme,
        hexColor: String?,
        modifier: Modifier = Modifier,
    ) {
        val backColor = hexColor?.toColor() ?: theme.getPreferredColor()
        BoxWithConstraints(modifier = modifier) {
            Box {
                CardBack(
                    theme = theme,
                    backColor = backColor,
                    rotation = rememberUpdatedState(0f),
                )
                ShimmerEffect()
            }
        }
    }

    @Composable
    private fun CardFacePreview(
        theme: CardSymbolTheme,
        modifier: Modifier = Modifier,
    ) {
        val suitColor = Color.Black // Spades is black

        BoxWithConstraints(modifier = modifier) {
            CardFace(
                rank = Rank.Ace,
                suit = Suit.Spades,
                suitColor = suitColor,
                theme = theme,
            )
        }
    }

    @Composable
    private fun FourColorPreview(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.background(Color.White).padding(PokerTheme.spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                SuitBox(Suit.Hearts, Color(0xFFD32F2F), Modifier.weight(1f))
                SuitBox(Suit.Diamonds, Color(0xFF1976D2), Modifier.weight(1f))
            }
            Row(modifier = Modifier.weight(1f)) {
                SuitBox(Suit.Clubs, Color(0xFF388E3C), Modifier.weight(1f))
                SuitBox(Suit.Spades, Color(0xFF212121), Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun ThirdEyePreview(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            CardBack(
                theme = CardBackTheme.GEOMETRIC,
                backColor = PokerTheme.colors.feltGreen,
                rotation = rememberUpdatedState(180f),
            )
            Icon(
                imageVector = AppIcons.Visibility,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = PokerTheme.colors.goldenYellow,
            )
        }
    }

    @Composable
    private fun SuitBox(
        suit: Suit,
        color: Color,
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    when (suit) {
                        Suit.Hearts -> suit.symbol
                        Suit.Diamonds -> suit.symbol
                        Suit.Clubs -> suit.symbol
                        Suit.Spades -> suit.symbol
                    },
                color = color,
                style = PokerTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}
