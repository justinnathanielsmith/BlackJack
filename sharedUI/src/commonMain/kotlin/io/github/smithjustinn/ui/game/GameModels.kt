package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.CardTheme
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
    val isPeekFeatureEnabled: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val cardTheme: CardTheme = CardTheme(),
    val areSuitsMultiColored: Boolean = false,
    val isThirdEyeEnabled: Boolean = false,
    val isHeatShieldEnabled: Boolean = false,
    val showComboExplosion: Boolean = false,
    val showTimeGain: Boolean = false,
    val showTimeLoss: Boolean = false,
    val timeGainAmount: Int = 0,
    val timeLossAmount: Long = 0,
    val isMegaBonus: Boolean = false,
    val isHeatMode: Boolean = false,
    val showWalkthrough: Boolean = false,
    val walkthroughStep: Int = 0,
    val hasUsedDoubleDownPeek: Boolean = false,
    val totalBalance: Long = 0,
    val isRewardedAdAvailable: Boolean = false,
    val totalGamesPlayed: Int = 0,
) {
    val isDoubleDownAvailable: Boolean
        get() {
            val unmatchedPairs = game.cards.count { !it.isMatched } / 2
            return isHeatMode &&
                !game.isDoubleDownActive &&
                !game.isGameOver &&
                unmatchedPairs >= 3
        }

    val canShowRewardedAd: Boolean
        get() = isRewardedAdAvailable && totalGamesPlayed >= MIN_GAMES_BEFORE_ADS

    companion object {
        private const val MIN_GAMES_BEFORE_ADS = 3
    }
}

sealed class GameUiEvent {
    data object PlayFlip : GameUiEvent()

    data object PlayMatch : GameUiEvent()

    data object PlayMismatch : GameUiEvent()

    data object PlayTheNuts : GameUiEvent()

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
