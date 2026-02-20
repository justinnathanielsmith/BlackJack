package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Configuration for game scoring and bonuses.
 *
 * All point values and thresholds are tunable for balancing gameplay.
 */
@Serializable
data class ScoringConfig(
    /** Base points awarded for each successful card match. */
    val baseMatchPoints: Int = DEFAULT_BASE_MATCH_POINTS,
    /** Additional points per combo level (stacks with each consecutive match). */
    val comboBonusPoints: Int = DEFAULT_COMBO_BONUS_POINTS,
    /** Bonus points per pair when completing the game quickly. */
    val timeBonusPerPair: Int = DEFAULT_TIME_BONUS_PER_PAIR,
    /** Points deducted per second over the target completion time. */
    val timePenaltyPerSecond: Int = DEFAULT_TIME_PENALTY_PER_SECOND,
    /** Base value for move efficiency calculation (higher rewards fewer moves). */
    val moveBonusMultiplier: Int = DEFAULT_MOVE_BONUS_MULTIPLIER,
    /** Combo streak required to activate heat mode visual effects. */
    val heatModeThreshold: Int = DEFAULT_HEAT_MODE_THRESHOLD,
    /** Points lost when failing a Double Down bet. */
    val doubleDownPenalty: Int = DEFAULT_DOUBLE_DOWN_PENALTY,
    /** Minimum combo streak for the "High Roller" achievement. */
    val highRollerThreshold: Int = DEFAULT_HIGH_ROLLER_THRESHOLD,
    /** Combo streak required for "The Nuts" achievement (very difficult). */
    val theNutsThreshold: Int = DEFAULT_THE_NUTS_THRESHOLD,
    /** Map of pair counts to initial time bank in Time Attack mode. */
    val timeAttackInitialTimeMap: Map<Int, Long> =
        mapOf(
            PAIR_COUNT_6 to INITIAL_TIME_25,
            PAIR_COUNT_8 to INITIAL_TIME_35,
            PAIR_COUNT_10 to INITIAL_TIME_45,
            PAIR_COUNT_12 to INITIAL_TIME_55,
        ),
    /** Base time gain on match in Time Attack. */
    val timeAttackBaseGain: Int = DEFAULT_TIME_ATTACK_BASE_GAIN,
    /** Bonus time per combo level in Time Attack. */
    val timeAttackComboBonusMultiplier: Int = DEFAULT_TIME_ATTACK_COMBO_BONUS_MULTIPLIER,
    /** Penalty in seconds for a mismatch in Time Attack. */
    val timeAttackMismatchPenalty: Long = DEFAULT_TIME_ATTACK_MISMATCH_PENALTY,
    /** Divisor used to determine "Pot Odds" comment timing. */
    val commentPotOddsDivisor: Int = DEFAULT_COMMENT_POT_ODDS_DIVISOR,
    /** Threshold for "Photographic" memory comment (moves per match). */
    val commentMovesPerMatchThreshold: Int = DEFAULT_COMMENT_MOVES_PER_MATCH_THRESHOLD,
    /** Number of matches required to bank the current pot. */
    val matchMilestoneInterval: Int = DEFAULT_MATCH_MILESTONE_INTERVAL,
    /** Percentage of the current pot lost on a mismatch (0.0 to 1.0). */
    val potMismatchPenalty: Double = DEFAULT_POT_MISMATCH_PENALTY,
) {
    init {
        require(baseMatchPoints > 0) { "Base match points must be positive" }
        require(heatModeThreshold > 0) { "Heat mode threshold must be positive" }
        require(theNutsThreshold >= highRollerThreshold) {
            "The Nuts threshold must be >= High Roller threshold"
        }
        require(potMismatchPenalty in 0.0..1.0) { "Pot mismatch penalty must be between 0.0 and 1.0" }
        require(timePenaltyPerSecond >= 0) { "Time penalty cannot be negative" }
        require(timeAttackMismatchPenalty >= 0) { "Time attack mismatch penalty cannot be negative" }
        require(comboBonusPoints >= 0) { "Combo bonus points cannot be negative" }
        require(doubleDownPenalty >= 0) { "Double down penalty cannot be negative" }
        require(moveBonusMultiplier >= 0) { "Move bonus multiplier cannot be negative" }
        require(commentPotOddsDivisor > 0) { "Pot odds divisor must be positive" }
        require(matchMilestoneInterval > 0) { "Match milestone interval must be positive" }
        require(timeBonusPerPair >= 0) { "Time bonus per pair cannot be negative" }
        require(timeAttackBaseGain >= 0) { "Time attack base gain cannot be negative" }
        require(timeAttackComboBonusMultiplier >= 0) { "Time attack combo bonus multiplier cannot be negative" }
        require(highRollerThreshold > 0) { "High roller threshold must be positive" }
    }

    companion object {
        const val DEFAULT_BASE_MATCH_POINTS = 100
        const val DEFAULT_COMBO_BONUS_POINTS = 50
        const val DEFAULT_TIME_BONUS_PER_PAIR = 50
        const val DEFAULT_TIME_PENALTY_PER_SECOND = 1
        const val DEFAULT_MOVE_BONUS_MULTIPLIER = 10000
        const val DEFAULT_HEAT_MODE_THRESHOLD = 3
        const val DEFAULT_DOUBLE_DOWN_PENALTY = 500
        const val DEFAULT_HIGH_ROLLER_THRESHOLD = 1
        const val DEFAULT_THE_NUTS_THRESHOLD = 6

        const val PAIR_COUNT_6 = 6
        const val PAIR_COUNT_8 = 8
        const val PAIR_COUNT_10 = 10
        const val PAIR_COUNT_12 = 12

        const val INITIAL_TIME_25 = 25L
        const val INITIAL_TIME_35 = 35L
        const val INITIAL_TIME_45 = 45L
        const val INITIAL_TIME_55 = 55L

        const val DEFAULT_TIME_ATTACK_BASE_GAIN = 3
        const val DEFAULT_TIME_ATTACK_COMBO_BONUS_MULTIPLIER = 2
        const val DEFAULT_TIME_ATTACK_MISMATCH_PENALTY = 2L
        const val DEFAULT_COMMENT_POT_ODDS_DIVISOR = 2
        const val DEFAULT_COMMENT_MOVES_PER_MATCH_THRESHOLD = 2

        const val DEFAULT_MATCH_MILESTONE_INTERVAL = 4
        const val DEFAULT_POT_MISMATCH_PENALTY = 0.25
    }
}
