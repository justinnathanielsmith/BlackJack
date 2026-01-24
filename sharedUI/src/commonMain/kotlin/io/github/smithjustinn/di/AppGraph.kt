package io.github.smithjustinn.di

import androidx.compose.runtime.staticCompositionLocalOf
import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.*
import io.github.smithjustinn.domain.usecases.stats.*
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService

/**
 * The primary entry point for the dependency graph.
 * This interface only exposes what is needed by the UI layer.
 */
interface AppGraph {
    val logger: Logger
    val audioService: AudioService
    val hapticsService: HapticsService
    val coroutineDispatchers: io.github.smithjustinn.utils.CoroutineDispatchers

    val gameStateRepository: GameStateRepository
    val settingsRepository: SettingsRepository
    val leaderboardRepository: LeaderboardRepository
    val gameStatsRepository: GameStatsRepository
    val dailyChallengeRepository: io.github.smithjustinn.data.repository.DailyChallengeRepository

    // Use Cases
    val startNewGameUseCase: StartNewGameUseCase
    val flipCardUseCase: FlipCardUseCase
    val resetErrorCardsUseCase: ResetErrorCardsUseCase
    val calculateFinalScoreUseCase: CalculateFinalScoreUseCase
    val getGameStatsUseCase: GetGameStatsUseCase
    val saveGameResultUseCase: SaveGameResultUseCase
    val getSavedGameUseCase: GetSavedGameUseCase
    val saveGameStateUseCase: SaveGameStateUseCase
    val clearSavedGameUseCase: ClearSavedGameUseCase
}

val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No AppGraph provided")
}
