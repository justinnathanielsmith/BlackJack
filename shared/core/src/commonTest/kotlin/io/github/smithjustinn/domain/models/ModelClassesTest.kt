package io.github.smithjustinn.domain.models

import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.comment_boom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Instant

class ModelClassesTest {

    @Test
    fun `GameMode values exist`() {
        assertNotNull(GameMode.valueOf("TIME_ATTACK"))
        assertNotNull(GameMode.valueOf("DAILY_CHALLENGE"))
        assertEquals(2, GameMode.entries.size)
    }

    @Test
    fun `GameStats properties`() {
        val stats = GameStats(8, 100, 60)
        assertEquals(8, stats.pairCount)
        assertEquals(100, stats.bestScore)
        assertEquals(60, stats.bestTimeSeconds)
    }

    @Test
    fun `LeaderboardEntry has correct properties`() {
        val entry = LeaderboardEntry(
            pairCount = 8,
            score = 100,
            timeSeconds = 60,
            moves = 20,
            timestamp = Instant.DISTANT_PAST
        )
        assertEquals(8, entry.pairCount)
        assertEquals(100, entry.score)
    }

    @Test
    fun `MatchComment properties`() {
        val comment = MatchComment(Res.string.comment_boom)
        assertEquals(Res.string.comment_boom, comment.res)
    }

    @Test
    fun `ScoreBreakdown default values`() {
        val breakdown = ScoreBreakdown()
        assertEquals(0, breakdown.basePoints)
        assertEquals(0, breakdown.timeBonus)
        assertEquals(0, breakdown.moveBonus)
        assertEquals(0, breakdown.comboBonus)
        assertEquals(0, breakdown.doubleDownBonus)
        assertEquals(0, breakdown.totalScore)
    }

    @Test
    fun `ScoringConfig default values`() {
        val config = ScoringConfig()
        assertEquals(100, config.baseMatchPoints)
        assertEquals(50, config.comboBonusPoints)
    }
    
    @Test
    fun `GameDomainEvent coverage`() {
        assertNotNull(GameDomainEvent.CardFlipped)
        assertNotNull(GameDomainEvent.MatchSuccess)
        assertNotNull(GameDomainEvent.TheNutsAchieved)
        assertNotNull(GameDomainEvent.MatchFailure)
        assertNotNull(GameDomainEvent.GameWon)
        assertNotNull(GameDomainEvent.GameOver)
    }
}
