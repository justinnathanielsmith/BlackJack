package io.github.smithjustinn.di

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.smithjustinn.screens.DifficultyScreenModel
import io.github.smithjustinn.screens.GameScreenModel
import io.github.smithjustinn.screens.StatsScreenModel
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.usecases.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.ClearSavedGameUseCase

interface AppGraph {
    val difficultyScreenModel: DifficultyScreenModel
    val gameScreenModel: GameScreenModel
    val statsScreenModel: StatsScreenModel
    val hapticsService: HapticsService
    val gameStatsRepository: GameStatsRepository
    val leaderboardRepository: LeaderboardRepository
    val gameStateRepository: GameStateRepository
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
