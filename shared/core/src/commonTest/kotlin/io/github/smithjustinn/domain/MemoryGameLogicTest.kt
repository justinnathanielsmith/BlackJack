package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.comment_bad_beat
import io.github.smithjustinn.resources.comment_boom
import io.github.smithjustinn.resources.comment_eagle_eyes
import io.github.smithjustinn.resources.comment_full_house
import io.github.smithjustinn.resources.comment_great_find
import io.github.smithjustinn.resources.comment_on_a_roll
import io.github.smithjustinn.resources.comment_one_more
import io.github.smithjustinn.resources.comment_photographic
import io.github.smithjustinn.resources.comment_pot_odds
import io.github.smithjustinn.resources.comment_sharp
import io.github.smithjustinn.resources.comment_you_got_it
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
        assertEquals(GameMode.TIME_ATTACK, state.mode)

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
        val state = MemoryGameLogic.createInitialState(pairCount)

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
        val secondCard =
            initialState.cards.first {
                it.id != firstCard.id &&
                    it.suit == firstCard.suit &&
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
    fun `applyFinalBonuses should calculate score correctly for non-Time Attack mode`() {
        val pairCount = 2
        var state = MemoryGameLogic.createInitialState(pairCount, mode = GameMode.DAILY_CHALLENGE)

        // Manually set the state to win with some stats
        state =
            state.copy(
                isGameWon = true,
                moves = 2, // Perfect efficiency for 2 pairs
                score = 40, // 2 matches * 20 points
                totalBasePoints = 40,
            )

        // 10 seconds elapsed
        val finalState = MemoryGameLogic.applyFinalBonuses(state, 10)

        assertTrue(finalState.score > state.score)
        assertNotNull(finalState.scoreBreakdown)
        assertEquals(40, finalState.scoreBreakdown.basePoints)
        assertTrue(finalState.scoreBreakdown.timeBonus >= 0)
        assertTrue(finalState.scoreBreakdown.moveBonus > 0)
        assertEquals(finalState.score, finalState.scoreBreakdown.totalScore)
    }

    @Test
    fun `applyFinalBonuses should calculate score correctly for Time Attack mode`() {
        val pairCount = 2
        var state = MemoryGameLogic.createInitialState(pairCount, mode = GameMode.TIME_ATTACK)

        state =
            state.copy(
                isGameWon = true,
                moves = 2,
                score = 40,
                totalBasePoints = 40,
            )

        val remainingTime = 15L
        val finalState = MemoryGameLogic.applyFinalBonuses(state, remainingTime)

        assertEquals(40, finalState.scoreBreakdown.basePoints)
        assertEquals((remainingTime * 10).toInt(), finalState.scoreBreakdown.timeBonus)
        assertTrue(finalState.scoreBreakdown.moveBonus > 0)
        assertEquals(finalState.score, finalState.scoreBreakdown.totalScore)
    }

    @Test
    fun `calculateInitialTime should return correct values for difficulties`() {
        assertEquals(25L, TimeAttackLogic.calculateInitialTime(6))
        assertEquals(35L, TimeAttackLogic.calculateInitialTime(8))
        assertEquals(45L, TimeAttackLogic.calculateInitialTime(10))
        assertEquals(55L, TimeAttackLogic.calculateInitialTime(12))
        assertEquals(55L, TimeAttackLogic.calculateInitialTime(12))
    }

    @Test
    fun `calculateTimeGain should return correct values including combo bonus`() {
        // Base gain (combo 1)
        assertEquals(3, TimeAttackLogic.calculateTimeGain(1))
        // Combo 2
        assertEquals(5, TimeAttackLogic.calculateTimeGain(2))
        // Combo 3
        assertEquals(7, TimeAttackLogic.calculateTimeGain(3))
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
        val card4 =
            initialState.cards.first {
                it.id != card3.id &&
                    (it.suit != card3.suit || it.rank != card3.rank) &&
                    !it.isMatched &&
                    it.id != card1.id &&
                    it.id != card2.id
            }

        // First match: combo becomes 1 (streak 1)
        val (s1, _) = MemoryGameLogic.flipCard(initialState, card1.id)
        val (s2, _) = MemoryGameLogic.flipCard(s1, card2.id)
        assertEquals(1, s2.comboMultiplier)

        // Mis-match: combo resets to 0? Or 0?
        // Logic: comboMultiplier = 0 on failure.
        // Wait, line 190 in Logic: comboMultiplier = 0.
        // But the test asserted 1?
        // Let's verify Logic Failure: comboMultiplier = 0.
        // Line 44 in Core Test expects 0.
        // This test expected 1? Maybe it thought 1 is min?
        // Logic says 0. So assertion '1' on line 254 was prob wrong or I misread.
        // Wait, if it resets to 1 in old logic?
        // Old Logic: comboMultiplier = 0.
        // Test 254: assertEquals(1, s4.comboMultiplier).
        // If test expects 1, maybe it thinks min is 1.
        // But Logic says 0.
        // I should set it to expect 0.

        val (s3, _) = MemoryGameLogic.flipCard(s2, card3.id)
        val (s4, _) = MemoryGameLogic.flipCard(s3, card4.id)

        assertEquals(0, s4.comboMultiplier)
    }

    @Test
    fun `comboMultiplier should increment after successive matches`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)

        // Find two pairs
        val pair1Card1 = initialState.cards[0]
        val pair1Card2 =
            initialState.cards.first {
                it.id != pair1Card1.id &&
                    it.suit == pair1Card1.suit &&
                    it.rank == pair1Card1.rank
            }

        val pair2Card1 = initialState.cards.first { !it.isMatched && it.id != pair1Card1.id && it.id != pair1Card2.id }
        val pair2Card2 =
            initialState.cards.first {
                it.id != pair2Card1.id &&
                    it.suit == pair2Card1.suit &&
                    it.rank == pair2Card1.rank
            }

        // Match 1
        val (s1, _) = MemoryGameLogic.flipCard(initialState, pair1Card1.id)
        val (s2, _) = MemoryGameLogic.flipCard(s1, pair1Card2.id)
        assertEquals(1, s2.comboMultiplier)

        // Match 2
        val (s3, _) = MemoryGameLogic.flipCard(s2, pair2Card1.id)
        val (s4, _) = MemoryGameLogic.flipCard(s3, pair2Card2.id)
        assertEquals(2, s4.comboMultiplier)
    }

    @Test
    fun `calculateInitialTime should fallback for unknown pair count`() {
        // Fallback is pairCount * 4
        // Pair count 20 -> 80L
        assertEquals(80L, TimeAttackLogic.calculateInitialTime(20))
    }

    @Test
    fun `TIME_PENALTY_MISMATCH should be 2 seconds`() {
        assertEquals(2L, TimeAttackLogic.TIME_PENALTY_MISMATCH)
    }

    @Test
    fun `match comments coverage`() {
        // We need to hit specific branches in generateMatchComment
        // It is private, so we trigger it via flipCard -> handleMatchSuccess

        // 1. Halfway: matchesFound == totalPairs / 2
        // Total 4 pairs. Match 2 pairs.
        val pairCount = 4
        var state = MemoryGameLogic.createInitialState(pairCount)
        // Set up state to be 1 match away from halfway (1 match done)
        // Halfway is 2. So we need 1 match already.
        // But logic calculates matches AFTER the current one.
        // So we want existing matches = 1. New match -> 2.

        // Let's just mock the state to be almost there
        // Pair 1 is matched. Pair 2 is about to be matched.
        val p1c1 = state.cards[0]
        val p1c2 = state.cards.first { it.id != p1c1.id && it.suit == p1c1.suit && it.rank == p1c1.rank }

        state =
            state.copy(
                cards =
                    state.cards
                        .map {
                            if (it.id == p1c1.id || it.id == p1c2.id) it.copy(isMatched = true) else it
                        }.toImmutableList(),
                moves = 10, // Ensure moves > matches * 2 to avoid photographic
            )

        // Now find Pair 2
        val p2c1 = state.cards.first { !it.isMatched }
        val p2c2 =
            state.cards.first {
                !it.isMatched && it.suit == p2c1.suit && it.rank == p2c1.rank && it.id != p2c1.id
            }

        var (s1, _) = MemoryGameLogic.flipCard(state, p2c1.id)
        var (s2, _) = MemoryGameLogic.flipCard(s1, p2c2.id)

        // matchesFound should be 2. Total 4. Halfway.
        assertEquals(Res.string.comment_pot_odds, s2.matchComment?.res)

        // 2. One More: matchesFound == totalPairs - 1
        // Total 4. Need 3 matches.
        state = MemoryGameLogic.createInitialState(4)
        // Mark 2 pairs matched.
        val cards = state.cards.toMutableList()
        val pairs = cards.groupBy { it.suit to it.rank }.values.toList()

        // Match pair 0 and 1
        val pair0 = pairs[0]
        val pair1 = pairs[1]
        val pair2 = pairs[2] // Target

        state =
            state.copy(
                cards =
                    state.cards
                        .map { c ->
                            if (pair0.any { it.id == c.id } ||
                                pair1.any { it.id == c.id }
                            ) {
                                c.copy(isMatched = true)
                            } else {
                                c
                            }
                        }.toImmutableList(),
                lastMatchedIds = persistentListOf(),
                moves = 20,
                comboMultiplier = 1,
            )

        // Match Pair 2 -> Matches = 3. Total 4. One more to go.
        s1 = MemoryGameLogic.flipCard(state, pair2[0].id).first
        s2 = MemoryGameLogic.flipCard(s1, pair2[1].id).first
        assertEquals(Res.string.comment_one_more, s2.matchComment?.res)

        // 3. Photographic (moves <= matches * 2)
        state = MemoryGameLogic.createInitialState(4)
        // No matches. Match first pair with 2 moves.
        val pairs3 =
            state.cards
                .groupBy { it.suit to it.rank }
                .values
                .toList()
        s1 = MemoryGameLogic.flipCard(state, pairs3[0][0].id).first
        MemoryGameLogic.flipCard(s1, pairs3[0][1].id).first
        // moves=1. matches=1. 1 <= 2.

        // So to hit Photographic we need matches > 1, not halfway, not one more.
        // Total 10 pairs. Match 2. (Matches=2. Total=10. Not 1. Not 5. Not 9.)
        state = MemoryGameLogic.createInitialState(10)
        val pairs4 =
            state.cards
                .groupBy { it.suit to it.rank }
                .values
                .toList()

        state =
            state.copy(
                cards =
                    state.cards
                        .map { c ->
                            if (pairs4[0].any { it.id == c.id }) c.copy(isMatched = true) else c
                        }.toImmutableList(),
                lastMatchedIds = persistentListOf(),
                moves = 2,
                comboMultiplier = 1,
            )
        // Match pair 1
        s1 = MemoryGameLogic.flipCard(state, pairs4[1][0].id).first
        s2 = MemoryGameLogic.flipCard(s1, pairs4[1][1].id).first

        assertEquals(Res.string.comment_photographic, s2.matchComment?.res)

        // 4. Else (Random)
        state = MemoryGameLogic.createInitialState(10)
        val pairs5 =
            state.cards
                .groupBy { it.suit to it.rank }
                .values
                .toList()
        state =
            state.copy(
                cards =
                    state.cards
                        .map { c ->
                            if (pairs5[0].any { it.id == c.id } ||
                                pairs5[1].any { it.id == c.id }
                            ) {
                                c.copy(isMatched = true)
                            } else {
                                c
                            }
                        }.toImmutableList(),
                lastMatchedIds = persistentListOf(),
                moves = 50, // Lots of moves
                comboMultiplier = 1,
            )
        // Match pair 2
        s1 = MemoryGameLogic.flipCard(state, pairs5[2][0].id).first
        s2 = MemoryGameLogic.flipCard(s1, pairs5[2][1].id).first

        // Should be one of the random ones
        val randomComments =
            listOf(
                Res.string.comment_great_find,
                Res.string.comment_you_got_it,
                Res.string.comment_boom,
                Res.string.comment_eagle_eyes,
                Res.string.comment_sharp,
                Res.string.comment_on_a_roll,
                Res.string.comment_full_house,
                Res.string.comment_bad_beat,
            )
        assertTrue(randomComments.contains(s2.matchComment?.res))
    }
}
