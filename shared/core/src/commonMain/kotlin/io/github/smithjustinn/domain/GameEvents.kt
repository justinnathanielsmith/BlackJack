package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState

sealed class GameAction {
    data class StartGame(
        val gameState: MemoryGameState? = null,
    ) : GameAction()

    data class FlipCard(
        val cardId: Int,
    ) : GameAction()

    data object DoubleDown : GameAction()

    data class ScanCards(
        val durationMs: Long,
    ) : GameAction()

    data object ProcessMismatch : GameAction()

    data object Tick : GameAction()

    data object Restart : GameAction()

    data object ClearComment : GameAction()
}

sealed class GameEffect {
    data object PlayFlipSound : GameEffect()

    data object PlayWinSound : GameEffect()

    data object PlayLoseSound : GameEffect()

    data object PlayTheNutsSound : GameEffect()

    data object VibrateMatch : GameEffect()

    data object VibrateHeat : GameEffect()

    data object HeatShieldUsed : GameEffect()

    data class TimerUpdate(
        val seconds: Long,
    ) : GameEffect()

    data class TimeGain(
        val amount: Int,
    ) : GameEffect()

    data class TimeLoss(
        val amount: Int,
    ) : GameEffect()

    data object PlayMatchSound : GameEffect()

    data object VibrateMismatch : GameEffect()

    data object PlayMismatch : GameEffect()

    data object VibrateWarning : GameEffect()

    data object VibrateTick : GameEffect()

    data object GameOver : GameEffect()

    data class GameWon(
        val finalState: MemoryGameState,
    ) : GameEffect()
}
