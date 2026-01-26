package io.github.smithjustinn.androidApp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.ui.game.components.CardContent
import io.github.smithjustinn.ui.game.components.CardVisualState
import io.github.smithjustinn.ui.game.components.PlayingCard

@Preview(showBackground = true)
@Composable
private fun PlayingCardFaceUpPreview() {
    PlayingCard(
        content =
            CardContent(
                suit = Suit.Hearts,
                rank = Rank.Ace,
                visualState = CardVisualState(isFaceUp = true),
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PlayingCardFaceDownPreview() {
    PlayingCard(
        content =
            CardContent(
                suit = Suit.Spades,
                rank = Rank.King,
                visualState = CardVisualState(isFaceUp = false),
            ),
        backColor = Color.Red,
    )
}
