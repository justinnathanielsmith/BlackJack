package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraph
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.IosHapticsServiceImpl
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.IosAudioServiceImpl
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.AppDatabaseConstructor
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.SettingsDao
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStateRepositoryImpl
import io.github.smithjustinn.data.repositories.SettingsRepositoryImpl
import io.github.smithjustinn.domain.usecases.game.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.game.ClearSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.game.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.game.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.game.StartNewGameUseCase
import io.github.smithjustinn.ui.difficulty.DifficultyScreenModel
import io.github.smithjustinn.ui.game.GameScreenModel
import io.github.smithjustinn.ui.stats.StatsScreenModel
import io.github.smithjustinn.ui.settings.SettingsScreenModel
import platform.Foundation.NSHomeDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json

@DependencyGraph(AppScope::class)
interface IosAppGraph : AppGraph {
    override val logger: Logger
    override val difficultyScreenModel: DifficultyScreenModel
    override val gameScreenModel: GameScreenModel
    override val statsScreenModel: StatsScreenModel
    override val settingsScreenModel: SettingsScreenModel
    override val hapticsService: HapticsService
    override val audioService: AudioService
    override val gameStatsRepository: GameStatsRepository
    override val leaderboardRepository: LeaderboardRepository
    override val gameStateRepository: GameStateRepository
    override val settingsRepository: SettingsRepository
    override val startNewGameUseCase: StartNewGameUseCase
    override val flipCardUseCase: FlipCardUseCase
    override val resetErrorCardsUseCase: ResetErrorCardsUseCase
    override val calculateFinalScoreUseCase: CalculateFinalScoreUseCase
    override val getSavedGameUseCase: GetSavedGameUseCase
    override val saveGameStateUseCase: SaveGameStateUseCase
    override val clearSavedGameUseCase: ClearSavedGameUseCase

    @Provides
    @SingleIn(AppScope::class)
    fun provideHapticsService(impl: IosHapticsServiceImpl): HapticsService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideAudioService(impl: IosAudioServiceImpl): AudioService = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase {
        val dbFile = NSHomeDirectory() + "/memory_match.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() }
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideGameStatsDao(database: AppDatabase): GameStatsDao = database.gameStatsDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideLeaderboardDao(database: AppDatabase): LeaderboardDao = database.leaderboardDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideGameStateDao(database: AppDatabase): GameStateDao = database.gameStateDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideGameStatsRepository(impl: GameStatsRepositoryImpl): GameStatsRepository = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideLeaderboardRepository(impl: LeaderboardRepositoryImpl): LeaderboardRepository = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideGameStateRepository(impl: GameStateRepositoryImpl): GameStateRepository = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}

fun createIosGraph(): AppGraph = createGraph<IosAppGraph>()
