package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode

data class GameArgs(
    val pairCount: Int,
    val mode: GameMode,
    val difficulty: DifficultyType,
    val forceNewGame: Boolean,
    val seed: Long? = null,
) {
    companion object {
        const val MIN_PAIRS = 2
        const val MAX_PAIRS = 52
    }

    init {
        require(pairCount in MIN_PAIRS..MAX_PAIRS) {
            "pairCount must be between $MIN_PAIRS and $MAX_PAIRS"
        }
    }
}
