package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class CardBackTheme(
    val id: String,
) {
    GEOMETRIC("theme_standard"),
    CLASSIC("theme_classic"),
    PATTERN("theme_pattern"),
    POKER("theme_poker"),
    ;

    companion object {
        fun fromIdOrName(value: String): CardBackTheme = entries.find { it.id == value || it.name == value } ?: GEOMETRIC
    }
}

@Serializable
enum class CardSymbolTheme(
    val id: String,
) {
    CLASSIC("skin_classic"),
    MINIMAL("skin_minimal"),
    TEXT_ONLY("skin_text"),
    POKER("skin_poker"),
    ;

    companion object {
        fun fromIdOrName(value: String): CardSymbolTheme = entries.find { it.id == value || it.name == value } ?: CLASSIC
    }
}
