package io.github.smithjustinn.di

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.domain.repositories.DailyChallengeRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import io.github.smithjustinn.domain.services.AdService
import io.github.smithjustinn.domain.usecases.economy.EarnCurrencyUseCase
import io.github.smithjustinn.domain.usecases.game.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.game.ClearSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.game.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.game.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.game.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KoinAppGraph :
    AppGraph,
    KoinComponent {
    override val logger: Logger by inject()
    override val audioService: AudioService by inject()
    override val hapticsService: HapticsService by inject()
    override val adService: AdService by inject()
    override val coroutineDispatchers: CoroutineDispatchers by inject()
    override val applicationScope: CoroutineScope by inject()

    override val appDatabase: AppDatabase by inject()

    override val gameStateRepository: GameStateRepository by inject()
    override val settingsRepository: SettingsRepository by inject()
    override val leaderboardRepository: LeaderboardRepository by inject()
    override val gameStatsRepository: GameStatsRepository by inject()
    override val dailyChallengeRepository: DailyChallengeRepository by inject()
    override val playerEconomyRepository: PlayerEconomyRepository by inject()
    override val shopItemRepository: ShopItemRepository by inject()
    override val startNewGameUseCase: StartNewGameUseCase by inject()
    override val flipCardUseCase: FlipCardUseCase by inject()
    override val resetErrorCardsUseCase: ResetErrorCardsUseCase by inject()
    override val calculateFinalScoreUseCase: CalculateFinalScoreUseCase by inject()
    override val getGameStatsUseCase: GetGameStatsUseCase by inject()
    override val saveGameResultUseCase: SaveGameResultUseCase by inject()
    override val getSavedGameUseCase: GetSavedGameUseCase by inject()
    override val saveGameStateUseCase: SaveGameStateUseCase by inject()
    override val clearSavedGameUseCase: ClearSavedGameUseCase by inject()
    override val earnCurrencyUseCase: EarnCurrencyUseCase by inject()
}
