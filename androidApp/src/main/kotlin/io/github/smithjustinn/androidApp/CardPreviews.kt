package io.github.smithjustinn.androidApp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.ui.game.components.PlayingCard

@Preview(showBackground = true)
@Composable
fun PlayingCardFaceUpPreview() {
    PlayingCard(
        suit = Suit.Hearts,
        rank = Rank.Ace,
        isFaceUp = true
    )
}

@Preview(showBackground = true)
@Composable
fun PlayingCardFaceDownPreview() {
    PlayingCard(
        suit = Suit.Spades,
        rank = Rank.King,
        isFaceUp = false,
        backColor = Color.Red
    )
}
