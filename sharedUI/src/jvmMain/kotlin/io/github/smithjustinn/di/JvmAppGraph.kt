package io.github.smithjustinn.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraph
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.JvmHapticsServiceImpl
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.JvmAudioServiceImpl
import io.github.smithjustinn.data.local.AppDatabase
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
import io.github.smithjustinn.domain.usecases.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.ClearSavedGameUseCase
import io.github.smithjustinn.ui.difficulty.DifficultyScreenModel
import io.github.smithjustinn.ui.game.GameScreenModel
import io.github.smithjustinn.ui.stats.StatsScreenModel
import io.github.smithjustinn.ui.settings.SettingsScreenModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@DependencyGraph(AppScope::class)
interface JvmAppGraph : AppGraph {
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
    fun provideHapticsService(impl: JvmHapticsServiceImpl): HapticsService = impl

    @Provides
    fun provideAudioService(impl: JvmAudioServiceImpl): AudioService = impl

    @Provides
    fun provideDatabase(): AppDatabase {
        val dbFile = File(System.getProperty("user.home"), ".memory_match.db")
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    @Provides
    fun provideGameStatsDao(database: AppDatabase): GameStatsDao = database.gameStatsDao()

    @Provides
    fun provideLeaderboardDao(database: AppDatabase): LeaderboardDao = database.leaderboardDao()

    @Provides
    fun provideGameStateDao(database: AppDatabase): GameStateDao = database.gameStateDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideGameStatsRepository(impl: GameStatsRepositoryImpl): GameStatsRepository = impl

    @Provides
    fun provideLeaderboardRepository(impl: LeaderboardRepositoryImpl): LeaderboardRepository = impl

    @Provides
    fun provideGameStateRepository(impl: GameStateRepositoryImpl): GameStateRepository = impl

    @Provides
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Provides
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}

fun createJvmGraph(): AppGraph = createGraph<JvmAppGraph>()
