package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.GameMode

data class GameArgs(val pairCount: Int, val mode: GameMode, val forceNewGame: Boolean, val seed: Long? = null)
