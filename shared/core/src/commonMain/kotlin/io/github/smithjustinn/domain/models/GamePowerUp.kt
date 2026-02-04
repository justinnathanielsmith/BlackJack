package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class GamePowerUp(val id: String) {
    NONE("powerup_none"),
    TIME_BANK("powerup_timebank"),
    PEEK("powerup_peek"),
    ;

    companion object {
        fun fromIdOrName(value: String): GamePowerUp =
            entries.find { it.id == value || it.name == value } ?: NONE
    }
}
