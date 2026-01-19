package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Represents the overall state of the game screen, including the UI-only state.
 */
data class GameUIState(
    val game: MemoryGameState = MemoryGameState(),
    val elapsedTimeSeconds: Long = 0,
    val maxTimeSeconds: Long = 0,
    val bestScore: Int = 0,
    val bestTimeSeconds: Long = 0,
    val showComboExplosion: Boolean = false,
    val isNewHighScore: Boolean = false,
    val isPeeking: Boolean = false,
    val peekCountdown: Int = 0,
    val isPeekFeatureEnabled: Boolean = true,
    val showTimeGain: Boolean = false,
    val timeGainAmount: Int = 0,
    val showTimeLoss: Boolean = false,
    val timeLossAmount: Long = 0,
    val isMegaBonus: Boolean = false,
    val showWalkthrough: Boolean = false,
    val walkthroughStep: Int = 0,
    val isMusicEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false
)
