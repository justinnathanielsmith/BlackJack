package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MatchComment
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

/**
 * Generates poker-themed comments based on game events.
 */
object GameCommentGenerator {
    private val doubleDownComments =
        listOf(
            Res.string.comment_all_in,
            Res.string.comment_high_roller,
            Res.string.comment_stacking_chips,
            Res.string.comment_heater_active,
            Res.string.comment_pocket_aces,
            Res.string.comment_royal_flush,
            Res.string.comment_ship_it,
        )

    private val efficientComments =
        listOf(
            Res.string.comment_photographic,
            Res.string.comment_reading_tells,
            Res.string.comment_eagle_eyes,
        )

    private val randomComments =
        listOf(
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

    fun generateMatchComment(
        moves: Int,
        matchesFound: Int,
        totalPairs: Int,
        comboMultiplier: Int,
        config: ScoringConfig,
        isDoubleDownActive: Boolean = false,
    ): MatchComment {
        val res =
            when {
                matchesFound == totalPairs / config.commentPotOddsDivisor -> Res.string.comment_pot_odds
                matchesFound == totalPairs - 1 ->
                    listOf(Res.string.comment_one_more, Res.string.comment_river_magic).random()
                isDoubleDownActive -> doubleDownComments.random()
                comboMultiplier >= config.heatModeThreshold -> Res.string.comment_heater_active
                moves <= matchesFound * 2 && matchesFound > 1 -> efficientComments.random()
                else -> randomComments.random()
            }

        return MatchComment(res)
    }
}
