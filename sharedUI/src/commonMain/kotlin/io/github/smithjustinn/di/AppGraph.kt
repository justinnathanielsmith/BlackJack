package io.github.smithjustinn.di

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.smithjustinn.screens.DifficultyScreenModel
import io.github.smithjustinn.screens.GameScreenModel
import io.github.smithjustinn.screens.StatsScreenModel
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository

interface AppGraph {
    val difficultyScreenModel: DifficultyScreenModel
    val gameScreenModel: GameScreenModel
    val statsScreenModel: StatsScreenModel
    val hapticsService: HapticsService
    val gameStatsRepository: GameStatsRepository
    val leaderboardRepository: LeaderboardRepository
    val gameStateRepository: GameStateRepository
}

val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No AppGraph provided")
}
