package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a saved game state with elapsed time.
 * Replaces the generic Pair<MemoryGameState, Long> for better type safety and readability.
 */
@Serializable
data class SavedGame(
    val gameState: MemoryGameState,
    val elapsedTimeSeconds: Long,
) {
    init {
        require(elapsedTimeSeconds >= 0) { "Elapsed time cannot be negative" }
    }
}
