package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.MemoryGameState

data class GameUIState(
    val game: MemoryGameState = MemoryGameState(),
    val elapsedTimeSeconds: Long = 0,
    val maxTimeSeconds: Long = 0,
    val bestScore: Int = 0,
    val bestTimeSeconds: Long = 0,
    val isNewHighScore: Boolean = false,
    val isPeeking: Boolean = false,
    val peekCountdown: Int = 0,
    val isPeekFeatureEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false,
    val showComboExplosion: Boolean = false,
    val showTimeGain: Boolean = false,
    val showTimeLoss: Boolean = false,
    val timeGainAmount: Int = 0,
    val timeLossAmount: Long = 0,
    val isMegaBonus: Boolean = false,
    val isHeatMode: Boolean = false,
    val showWalkthrough: Boolean = false,
    val walkthroughStep: Int = 0,
)

sealed class GameUiEvent {
    data object PlayFlip : GameUiEvent()
    data object PlayMatch : GameUiEvent()
    data object PlayMismatch : GameUiEvent()
    data object PlayWin : GameUiEvent()
    data object PlayLose : GameUiEvent()
    data object PlayHighScore : GameUiEvent()
    data object PlayDeal : GameUiEvent()
    data object VibrateMatch : GameUiEvent()
    data object VibrateMismatch : GameUiEvent()
    data object VibrateTick : GameUiEvent()
    data object VibrateWarning : GameUiEvent()
    data object VibrateHeat : GameUiEvent()
}
