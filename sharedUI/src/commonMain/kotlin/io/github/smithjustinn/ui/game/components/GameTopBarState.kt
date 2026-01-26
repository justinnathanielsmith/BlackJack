package io.github.smithjustinn.ui.game.components

import io.github.smithjustinn.domain.models.GameMode

data class GameTopBarState(
    val time: Long,
    val mode: GameMode = GameMode.STANDARD,
    val maxTime: Long = 0,
    val showTimeGain: Boolean = false,
    val timeGainAmount: Int = 0,
    val showTimeLoss: Boolean = false,
    val timeLossAmount: Long = 0,
    val isMegaBonus: Boolean = false,
    val compact: Boolean = false,
    val isAudioEnabled: Boolean = true,
)
