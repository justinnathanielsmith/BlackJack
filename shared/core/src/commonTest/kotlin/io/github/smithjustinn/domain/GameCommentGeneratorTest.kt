package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.comment_all_in
import io.github.smithjustinn.resources.comment_bad_beat
import io.github.smithjustinn.resources.comment_boom
import io.github.smithjustinn.resources.comment_check_mate
import io.github.smithjustinn.resources.comment_eagle_eyes
import io.github.smithjustinn.resources.comment_flopped_a_set
import io.github.smithjustinn.resources.comment_full_house
import io.github.smithjustinn.resources.comment_great_find
import io.github.smithjustinn.resources.comment_grinding
import io.github.smithjustinn.resources.comment_heater_active
import io.github.smithjustinn.resources.comment_high_roller
import io.github.smithjustinn.resources.comment_no_bluff
import io.github.smithjustinn.resources.comment_on_a_roll
import io.github.smithjustinn.resources.comment_one_more
import io.github.smithjustinn.resources.comment_photographic
import io.github.smithjustinn.resources.comment_pocket_aces
import io.github.smithjustinn.resources.comment_poker_face
import io.github.smithjustinn.resources.comment_pot_odds
import io.github.smithjustinn.resources.comment_reading_tells
import io.github.smithjustinn.resources.comment_river_magic
import io.github.smithjustinn.resources.comment_royal_flush
import io.github.smithjustinn.resources.comment_sharp
import io.github.smithjustinn.resources.comment_ship_it
import io.github.smithjustinn.resources.comment_smooth_call
import io.github.smithjustinn.resources.comment_stacking_chips
import io.github.smithjustinn.resources.comment_you_got_it
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameCommentGeneratorTest {
    private val doubleDownComments =
        setOf(
            Res.string.comment_all_in,
            Res.string.comment_high_roller,
            Res.string.comment_stacking_chips,
            Res.string.comment_heater_active,
            Res.string.comment_pocket_aces,
            Res.string.comment_royal_flush,
            Res.string.comment_ship_it,
        )

    private val efficientComments =
        setOf(
            Res.string.comment_photographic,
            Res.string.comment_reading_tells,
            Res.string.comment_eagle_eyes,
        )

    private val randomComments =
        setOf(
            Res.string.comment_great_find,
            Res.string.comment_you_got_it,
            Res.string.comment_boom,
            Res.string.comment_sharp,
            Res.string.comment_on_a_roll,
            Res.string.comment_full_house,
            Res.string.comment_bad_beat,
            Res.string.comment_flopped_a_set,
            Res.string.comment_smooth_call,
            Res.string.comment_poker_face,
            Res.string.comment_grinding,
            Res.string.comment_check_mate,
            Res.string.comment_no_bluff,
        )

    @Test
    fun generateMatchComment_returnsPotOddsComment_whenConditionMet() {
        // matchesFound == totalPairs / config.commentPotOddsDivisor
        // 4 == 8 / 2
        val config = ScoringConfig(commentPotOddsDivisor = 2)
        val result =
            GameCommentGenerator.generateMatchComment(
                moves = 10,
                matchesFound = 4,
                totalPairs = 8,
                comboMultiplier = 1,
                config = config,
                isDoubleDownActive = false,
            )

        assertEquals(Res.string.comment_pot_odds, result.res)
    }

    @Test
    fun generateMatchComment_returnsRiverComment_whenOneMatchLeft() {
        // matchesFound == totalPairs - 1
        val config = ScoringConfig()
        val result =
            GameCommentGenerator.generateMatchComment(
                moves = 10,
                matchesFound = 7,
                totalPairs = 8,
                comboMultiplier = 1,
                config = config,
                isDoubleDownActive = false,
            )

        assertContains(listOf(Res.string.comment_one_more, Res.string.comment_river_magic), result.res)
    }

    @Test
    fun generateMatchComment_returnsDoubleDownComment_whenDoubleDownActive() {
        val config = ScoringConfig()
        val result =
            GameCommentGenerator.generateMatchComment(
                moves = 10,
                matchesFound = 1, // Avoid pot odds (1 != 8/2)
                totalPairs = 8,
                comboMultiplier = 1,
                config = config,
                isDoubleDownActive = true,
            )

        assertTrue(doubleDownComments.contains(result.res), "Result ${result.res} not in doubleDownComments")
    }

    @Test
    fun generateMatchComment_returnsHeaterComment_whenHeatThresholdMet() {
        // comboMultiplier >= config.heatModeThreshold
        val config = ScoringConfig(heatModeThreshold = 3)
        val result =
            GameCommentGenerator.generateMatchComment(
                moves = 10,
                matchesFound = 1, // Avoid pot odds
                totalPairs = 8,
                comboMultiplier = 3,
                config = config,
                isDoubleDownActive = false,
            )

        assertEquals(Res.string.comment_heater_active, result.res)
    }

    @Test
    fun generateMatchComment_returnsEfficientComment_whenMovesAreLow() {
        // moves <= matchesFound * 2 && matchesFound > 1
        // 4 <= 2 * 2
        val config = ScoringConfig(commentPotOddsDivisor = 2)
        val result =
            GameCommentGenerator.generateMatchComment(
                moves = 4,
                matchesFound = 2, // Avoid pot odds (2 != 8/2 = 4)
                totalPairs = 8,
                comboMultiplier = 1,
                config = config,
                isDoubleDownActive = false,
            )

        assertTrue(efficientComments.contains(result.res), "Result ${result.res} not in efficientComments")
    }

    @Test
    fun generateMatchComment_returnsRandomComment_whenNoSpecialConditionMet() {
        val config = ScoringConfig(commentPotOddsDivisor = 2)
        val result =
            GameCommentGenerator.generateMatchComment(
                moves = 100,
                matchesFound = 2, // 2 != 4 (pot odds)
                totalPairs = 8,
                comboMultiplier = 1,
                config = config,
                isDoubleDownActive = false,
            )

        assertTrue(randomComments.contains(result.res), "Result ${result.res} not in randomComments")
    }
}
