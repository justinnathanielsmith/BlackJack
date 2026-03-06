package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.*
import kotlinx.collections.immutable.persistentListOf

fun main() {
    val cards = (0 until 52).map { id ->
        CardState(id = id, suit = Suit.HEARTS, rank = Rank.ACE, isFaceUp = false, isMatched = false)
    }
    var state = MemoryGameState(
        cards = kotlinx.collections.immutable.persistentListOf(*cards.toTypedArray()),
        pairCount = 26
    )

    // Warmup
    for (i in 0 until 1000) {
        MatchEvaluator.flipCard(state, 0)
    }

    val start = System.nanoTime()
    for (i in 0 until 100000) {
        val st1 = MatchEvaluator.flipCard(state, 0).first
        MatchEvaluator.flipCard(st1, 1)
    }
    val end = System.nanoTime()
    println("Time taken: ${(end - start) / 1_000_000} ms")
}
