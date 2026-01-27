package io.github.smithjustinn.domain.models

enum class CardBackTheme {
    GEOMETRIC,
    CLASSIC,
    PATTERN,
    POKER,
}

enum class CardSymbolTheme {
    CLASSIC,
    MINIMAL,
    TEXT_ONLY,
    POKER,
}

data class CardDisplaySettings(
    val backTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val symbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false,
)
