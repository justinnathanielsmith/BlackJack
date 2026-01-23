package io.github.smithjustinn.ui.start

import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme

data class DifficultyState(
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.defaultLevels[1],
    val difficulties: List<DifficultyLevel> = DifficultyLevel.defaultLevels,
    val selectedMode: GameMode = GameMode.STANDARD,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val hasSavedGame: Boolean = false,
    val savedGamePairCount: Int = 0,
    val savedGameMode: GameMode = GameMode.STANDARD
)
