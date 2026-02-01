package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.comment_all_in
import io.github.smithjustinn.resources.comment_bad_beat
import io.github.smithjustinn.resources.comment_boom
import io.github.smithjustinn.resources.comment_check_mate
import io.github.smithjustinn.resources.comment_eagle_eyes
import io.github.smithjustinn.resources.comment_first_match
import io.github.smithjustinn.resources.comment_flopped_a_set
import io.github.smithjustinn.resources.comment_full_house
import io.github.smithjustinn.resources.comment_great_find
import io.github.smithjustinn.resources.comment_grinding
import io.github.smithjustinn.resources.comment_halfway
import io.github.smithjustinn.resources.comment_heater_active
import io.github.smithjustinn.resources.comment_high_roller
import io.github.smithjustinn.resources.comment_incredible
import io.github.smithjustinn.resources.comment_no_bluff
import io.github.smithjustinn.resources.comment_on_a_roll
import io.github.smithjustinn.resources.comment_one_more
import io.github.smithjustinn.resources.comment_perfect
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
import io.github.smithjustinn.resources.comment_the_nuts
import io.github.smithjustinn.resources.comment_you_got_it
import kotlinx.collections.immutable.persistentListOf

/**
 * Generates comments based on game events.
 */
object GameCommentGenerator {
    private const val ONE_MORE_REMAINING = 1
    private const val FIRST_MATCH = 1

    fun generateMatchComment(
        moves: Int,
        matchesFound: Int,
        totalPairs: Int,
        combo: Int,
        config: ScoringConfig,
        isDoubleDownActive: Boolean = false,
    ): MatchComment {
        if (matchesFound == totalPairs) return MatchComment(Res.string.comment_perfect)

        return when {
            // Milestone: Last card (Highest Priority)
            matchesFound == totalPairs - ONE_MORE_REMAINING -> getNearEndComment()

            // High stakes: Double Down
            isDoubleDownActive -> getDoubleDownComment()

            // Milestone: First Match
            matchesFound == FIRST_MATCH -> getFirstMatchComment()

            // High Combos
            combo >= config.theNutsThreshold -> {
                MatchComment(Res.string.comment_the_nuts, persistentListOf(combo))
            }

            combo >= config.highRollerThreshold && combo > 1 -> getHighRollerComment(combo)

            // Heat Mode transition
            combo == config.heatModeThreshold - 1 -> {
                MatchComment(Res.string.comment_heater_active)
            }

            // Strategic milestones
            matchesFound == totalPairs / config.commentPotOddsDivisor -> {
                MatchComment(Res.string.comment_pot_odds)
            }

            matchesFound == totalPairs / 2 -> {
                MatchComment(Res.string.comment_halfway)
            }

            // Efficiency / Speed
            moves <= matchesFound * config.commentMovesPerMatchThreshold -> getEfficiencyComment()

            // Variety / Random Poker Themed
            else -> getRandomGenericComment()
        }
    }

    private fun getNearEndComment() = MatchComment(
        listOf(Res.string.comment_one_more, Res.string.comment_river_magic).random()
    )

    private fun getDoubleDownComment() = MatchComment(
        listOf(
            Res.string.comment_ship_it,
            Res.string.comment_stacking_chips,
            Res.string.comment_all_in,
        ).random()
    )

    private fun getFirstMatchComment() = MatchComment(
        listOf(Res.string.comment_first_match, Res.string.comment_pocket_aces).random()
    )

    private fun getHighRollerComment(combo: Int) = MatchComment(
        listOf(
            Res.string.comment_high_roller,
            Res.string.comment_royal_flush,
            Res.string.comment_incredible,
        ).random(),
        persistentListOf(combo)
    )

    private fun getEfficiencyComment() = MatchComment(
        listOf(
            Res.string.comment_photographic,
            Res.string.comment_reading_tells,
            Res.string.comment_eagle_eyes,
        ).random()
    )

    private fun getRandomGenericComment() = MatchComment(
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
        ).random()
    )
}
