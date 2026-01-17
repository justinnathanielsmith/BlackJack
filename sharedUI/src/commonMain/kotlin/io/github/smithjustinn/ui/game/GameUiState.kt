package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.GameMode
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
    val isPeekFeatureEnabled: Boolean = true,
    val showTimeGain: Boolean = false,
    val timeGainAmount: Int = 0,
    val showTimeLoss: Boolean = false,
    val timeLossAmount: Long = 0,
    val isMegaBonus: Boolean = false
)

/**
 * Sealed class representing user intents for the game screen.
 */
sealed class GameIntent {
    data class StartGame(
        val pairCount: Int,
        val forceNewGame: Boolean = false,
        val mode: GameMode = GameMode.STANDARD
    ) : GameIntent()
    data class FlipCard(val cardId: Int) : GameIntent()
    data object SaveGame : GameIntent()
}

/**
 * Sealed class representing one-time UI events triggered by the ViewModel.
 */
sealed class GameUiEvent {
    data object PlayFlip : GameUiEvent()
    data object PlayMatch : GameUiEvent()
    data object PlayMismatch : GameUiEvent()
    data object PlayWin : GameUiEvent()
    data object PlayDeal : GameUiEvent()
    data object VibrateMatch : GameUiEvent()
    data object VibrateMismatch : GameUiEvent()
}
