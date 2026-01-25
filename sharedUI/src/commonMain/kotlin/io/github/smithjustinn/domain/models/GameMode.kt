package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Defines the available game modes.
 */
@Serializable
enum class GameMode {
    STANDARD,
    TIME_ATTACK,
    DAILY_CHALLENGE,
}
