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
        fun fromIdOrName(value: String): CardBackTheme =
            entries.find { value.startsWith(it.id) || it.name == value } ?: GEOMETRIC
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
    NEON("skin_neon"),
    RETRO("skin_retro"),
    ELEGANT("skin_elegant"),
    CYBERPUNK("skin_cyberpunk"),
    PIXEL("skin_pixel"),
    FUTURISTIC("skin_futuristic"),
    ;

    companion object {
        fun fromIdOrName(value: String): CardSymbolTheme =
            entries.find { it.id == value || it.name == value } ?: CLASSIC
    }
}

@Serializable
data class CardTheme(
    val back: CardBackTheme = CardBackTheme.GEOMETRIC,
    val skin: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val backColorHex: String? = null,
)
