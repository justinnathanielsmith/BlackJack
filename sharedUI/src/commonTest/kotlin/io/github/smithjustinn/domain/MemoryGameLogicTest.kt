package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemoryGameLogicTest {

    @Test
    fun `createInitialState should create correct number of cards`() {
        val pairCount = 4
        val state = MemoryGameLogic.createInitialState(pairCount)

        assertEquals(pairCount * 2, state.cards.size)
        assertEquals(pairCount, state.pairCount)
        assertEquals(GameMode.STANDARD, state.mode)

        // Verify we have pairs
        val groups = state.cards.groupBy { it.suit to it.rank }
        assertEquals(pairCount, groups.size)
        groups.forEach { (_, cards) ->
            assertEquals(2, cards.size)
        }
    }

    @Test
    fun `createInitialState with Time Attack mode`() {
        val pairCount = 4
        val state = MemoryGameLogic.createInitialState(pairCount, mode = GameMode.TIME_ATTACK)

        assertEquals(GameMode.TIME_ATTACK, state.mode)
    }

    @Test
    fun `flipCard should flip a face-down card`() {
        val state = MemoryGameLogic.createInitialState(4)
        val cardId = state.cards[0].id

        val (newState, event) = MemoryGameLogic.flipCard(state, cardId)

        assertTrue(newState.cards.first { it.id == cardId }.isFaceUp)
        assertEquals(GameDomainEvent.CardFlipped, event)
    }

    @Test
    fun `flipCard should do nothing if card is already face-up`() {
        val initialState = MemoryGameLogic.createInitialState(4)
        val cardId = initialState.cards[0].id
        val (stateAfterFirstFlip, _) = MemoryGameLogic.flipCard(initialState, cardId)

        val (stateAfterSecondFlip, event) = MemoryGameLogic.flipCard(stateAfterFirstFlip, cardId)

        assertEquals(stateAfterFirstFlip, stateAfterSecondFlip)
        assertNull(event)
    }

    @Test
    fun `flipCard should detect match when two identical cards are flipped`() {
        val pairCount = 2
        val initialState = MemoryGameLogic.createInitialState(pairCount)

        // Find a pair
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards.first {
            it.id != firstCard.id && it.suit == firstCard.suit &&
                it.rank == firstCard.rank
        }

        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, event) = MemoryGameLogic.flipCard(state1, secondCard.id)

        assertEquals(GameDomainEvent.MatchSuccess, event)
        assertTrue(state2.cards.first { it.id == firstCard.id }.isMatched)
        assertTrue(state2.cards.first { it.id == secondCard.id }.isMatched)
        assertEquals(1, state2.moves)
        assertEquals(state2.config.baseMatchPoints, state2.score)
    }

    @Test
    fun `flipCard should detect failure when two different cards are flipped`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)

        // Find two different cards
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards.first { it.suit != firstCard.suit || it.rank != firstCard.rank }

        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, event) = MemoryGameLogic.flipCard(state1, secondCard.id)

        assertEquals(GameDomainEvent.MatchFailure, event)
        assertTrue(state2.cards.first { it.id == firstCard.id }.isError)
        assertTrue(state2.cards.first { it.id == secondCard.id }.isError)
        assertEquals(1, state2.moves)
        assertEquals(0, state2.score)
    }

    @Test
    fun `resetErrorCards should flip back error cards`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards.first { it.suit != firstCard.suit || it.rank != firstCard.rank }

        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, _) = MemoryGameLogic.flipCard(state1, secondCard.id)

        val state3 = MemoryGameLogic.resetErrorCards(state2)

        assertFalse(state3.cards.any { it.isError })
        assertFalse(state3.cards.any { it.isFaceUp })
    }

    @Test
    fun `game should be won when all pairs are matched`() {
        val pairCount = 1
        val initialState = MemoryGameLogic.createInitialState(pairCount)

        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards[1]

        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, event) = MemoryGameLogic.flipCard(state1, secondCard.id)

        assertEquals(GameDomainEvent.GameWon, event)
        assertTrue(state2.isGameWon)
        assertTrue(state2.isGameOver)
    }

    @Test
    fun `flipCard should do nothing if game is over`() {
        val state = MemoryGameLogic.createInitialState(1).copy(isGameOver = true)
        val cardId = state.cards[0].id

        val (newState, event) = MemoryGameLogic.flipCard(state, cardId)

        assertEquals(state, newState)
        assertNull(event)
    }

    @Test
    fun `applyFinalBonuses should calculate score correctly for Standard mode`() {
        val pairCount = 2
        var state = MemoryGameLogic.createInitialState(pairCount, mode = GameMode.STANDARD)

        // Manually set the state to win with some stats
        state = state.copy(
            isGameWon = true,
            moves = 2, // Perfect efficiency for 2 pairs
            score = 40, // 2 matches * 20 points
        )

        // 10 seconds elapsed
        val finalState = MemoryGameLogic.applyFinalBonuses(state, 10)

        assertTrue(finalState.score > state.score)
        assertNotNull(finalState.scoreBreakdown)
        assertEquals(40, finalState.scoreBreakdown.matchPoints)
        assertTrue(finalState.scoreBreakdown.timeBonus >= 0)
        assertTrue(finalState.scoreBreakdown.moveBonus > 0)
        assertEquals(finalState.score, finalState.scoreBreakdown.totalScore)
    }

    @Test
    fun `applyFinalBonuses should calculate score correctly for Time Attack mode`() {
        val pairCount = 2
        var state = MemoryGameLogic.createInitialState(pairCount, mode = GameMode.TIME_ATTACK)

        state = state.copy(
            isGameWon = true,
            moves = 2,
            score = 40,
        )

        val remainingTime = 15L
        val finalState = MemoryGameLogic.applyFinalBonuses(state, remainingTime)

        assertEquals(40, finalState.scoreBreakdown.matchPoints)
        assertEquals((remainingTime * 10).toInt(), finalState.scoreBreakdown.timeBonus)
        assertTrue(finalState.scoreBreakdown.moveBonus > 0)
        assertEquals(finalState.score, finalState.scoreBreakdown.totalScore)
    }

    @Test
    fun `calculateInitialTime should return correct values for difficulties`() {
        assertEquals(25L, MemoryGameLogic.calculateInitialTime(6))
        assertEquals(35L, MemoryGameLogic.calculateInitialTime(8))
        assertEquals(45L, MemoryGameLogic.calculateInitialTime(10))
        assertEquals(55L, MemoryGameLogic.calculateInitialTime(12))
        assertEquals(65L, MemoryGameLogic.calculateInitialTime(14))
        assertEquals(75L, MemoryGameLogic.calculateInitialTime(16))
    }

    @Test
    fun `calculateTimeGain should return correct values including combo bonus`() {
        // Base gain (combo 1)
        assertEquals(3, MemoryGameLogic.calculateTimeGain(1))
        // Combo 2
        assertEquals(4, MemoryGameLogic.calculateTimeGain(2))
        // Combo 3
        assertEquals(5, MemoryGameLogic.calculateTimeGain(3))
    }

    @Test
    fun `comboMultiplier should reset after mismatch`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)

        // Find a pair
        val card1 = initialState.cards[0]
        val card2 = initialState.cards.first { it.id != card1.id && it.suit == card1.suit && it.rank == card1.rank }

        // Find a card that is NOT card1/card2 and its counterpart is NOT card3
        val card3 = initialState.cards.first { it.id != card1.id && it.id != card2.id }
        val card4 = initialState.cards.first {
            it.id != card3.id && (it.suit != card3.suit || it.rank != card3.rank) &&
                !it.isMatched &&
                it.id != card1.id &&
                it.id != card2.id
        }

        // First match: combo becomes 2
        val (s1, _) = MemoryGameLogic.flipCard(initialState, card1.id)
        val (s2, _) = MemoryGameLogic.flipCard(s1, card2.id)
        assertEquals(2, s2.comboMultiplier)

        // Mis-match: combo resets to 1
        val (s3, _) = MemoryGameLogic.flipCard(s2, card3.id)
        val (s4, _) = MemoryGameLogic.flipCard(s3, card4.id)

        assertEquals(1, s4.comboMultiplier)
    }

    @Test
    fun `comboMultiplier should increment after successive matches`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)

        // Find two pairs
        val pair1Card1 = initialState.cards[0]
        val pair1Card2 = initialState.cards.first {
            it.id != pair1Card1.id && it.suit == pair1Card1.suit &&
                it.rank == pair1Card1.rank
        }

        val pair2Card1 = initialState.cards.first { !it.isMatched && it.id != pair1Card1.id && it.id != pair1Card2.id }
        val pair2Card2 = initialState.cards.first {
            it.id != pair2Card1.id && it.suit == pair2Card1.suit &&
                it.rank == pair2Card1.rank
        }

        // Match 1
        val (s1, _) = MemoryGameLogic.flipCard(initialState, pair1Card1.id)
        val (s2, _) = MemoryGameLogic.flipCard(s1, pair1Card2.id)
        assertEquals(2, s2.comboMultiplier)

        // Match 2
        val (s3, _) = MemoryGameLogic.flipCard(s2, pair2Card1.id)
        val (s4, _) = MemoryGameLogic.flipCard(s3, pair2Card2.id)
        assertEquals(3, s4.comboMultiplier)
    }

    @Test
    fun `TIME_PENALTY_MISMATCH should be 2 seconds`() {
        assertEquals(2L, MemoryGameLogic.TIME_PENALTY_MISMATCH)
    }
}
