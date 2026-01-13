package io.github.smithjustinn.components.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardState

@Composable
fun GameGrid(
    cards: List<CardState>,
    onCardClick: (Int) -> Unit,
    isPeeking: Boolean = false
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards, key = { it.id }) { card ->
            PlayingCard(
                suit = card.suit,
                rank = card.rank,
                isFaceUp = card.isFaceUp || isPeeking,
                isMatched = card.isMatched,
                isError = card.isError,
                onClick = { onCardClick(card.id) }
            )
        }
    }
}
