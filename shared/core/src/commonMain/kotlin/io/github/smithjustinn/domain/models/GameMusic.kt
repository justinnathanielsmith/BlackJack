package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class GameMusic(
    val id: String,
) {
    DEFAULT("music_default"),
    ;

    companion object {
        fun fromIdOrName(value: String): GameMusic = entries.find { it.id == value || it.name == value } ?: DEFAULT
    }
}
