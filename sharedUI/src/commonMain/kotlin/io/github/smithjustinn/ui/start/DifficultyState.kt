package io.github.smithjustinn.ui.start

import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode

data class DifficultyState(
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.defaultLevels[1],
    val difficulties: List<DifficultyLevel> = DifficultyLevel.defaultLevels,
    val selectedMode: GameMode = GameMode.STANDARD,
    val cardSettings: CardDisplaySettings = CardDisplaySettings(),
    val hasSavedGame: Boolean = false,
    val savedGamePairCount: Int = 0,
    val savedGameMode: GameMode = GameMode.STANDARD,
    val isDailyChallengeCompleted: Boolean = false,
    val shouldAnimateEntrance: Boolean = true,
)
