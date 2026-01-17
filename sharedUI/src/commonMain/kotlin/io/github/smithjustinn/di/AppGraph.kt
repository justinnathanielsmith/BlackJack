package io.github.smithjustinn.di

import androidx.compose.runtime.staticCompositionLocalOf
import co.touchlab.kermit.Logger
import io.github.smithjustinn.ui.difficulty.DifficultyScreenModel
import io.github.smithjustinn.ui.game.GameScreenModel
import io.github.smithjustinn.ui.stats.StatsScreenModel
import io.github.smithjustinn.ui.settings.SettingsScreenModel
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.game.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.game.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.game.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.game.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.game.ClearSavedGameUseCase

interface AppGraph {
    val logger: Logger
    val difficultyScreenModel: DifficultyScreenModel
    val gameScreenModel: GameScreenModel
    val statsScreenModel: StatsScreenModel
    val settingsScreenModel: SettingsScreenModel
    val hapticsService: HapticsService
    val audioService: AudioService
    val gameStatsRepository: GameStatsRepository
    val leaderboardRepository: LeaderboardRepository
    val gameStateRepository: GameStateRepository
    val settingsRepository: SettingsRepository
    val startNewGameUseCase: StartNewGameUseCase
    val flipCardUseCase: FlipCardUseCase
    val resetErrorCardsUseCase: ResetErrorCardsUseCase
    val calculateFinalScoreUseCase: CalculateFinalScoreUseCase
    val getSavedGameUseCase: GetSavedGameUseCase
    val saveGameStateUseCase: SaveGameStateUseCase
    val clearSavedGameUseCase: ClearSavedGameUseCase
}

val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No AppGraph provided")
}
