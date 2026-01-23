package io.github.smithjustinn.ui.settings

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme

data class SettingsUIState(
    val isPeekEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isWalkthroughCompleted: Boolean = false,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 1.0f,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false
)

sealed class SettingsUiEvent {
    data object PlayClick : SettingsUiEvent()
}
