package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test

class MatchEvaluatorPerfJvmTest {
    @Test
    fun benchmarkFlip() {
        val cards =
            (0 until 104).map { id ->
                CardState(id = id, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = false, isMatched = false)
            }
        val state =
            MemoryGameState(
                cards = persistentListOf(*cards.toTypedArray()),
                pairCount = 52,
            )

        // Warmup
        for (i in 0 until 1000) {
            MatchEvaluator.flipCard(state, 0)
        }

        val start = System.currentTimeMillis()
        var c = 0
        for (i in 0 until 100000) {
            val (st1, _) = MatchEvaluator.flipCard(state, 0)
            MatchEvaluator.flipCard(st1, 1)
            c++
        }
        val end = System.currentTimeMillis()
        println("BENCHMARK_RESULT: Time taken: " + (end - start) + " ms, loops: " + c)
    }
}
