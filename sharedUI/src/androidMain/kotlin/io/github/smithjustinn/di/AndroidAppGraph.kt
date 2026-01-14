package io.github.smithjustinn.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.services.AndroidHapticsServiceImpl
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
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@DependencyGraph(AppScope::class)
interface AndroidAppGraph : AppGraph {
    override val logger: Logger
    override val difficultyScreenModel: DifficultyScreenModel
    override val gameScreenModel: GameScreenModel
    override val statsScreenModel: StatsScreenModel
    override val settingsScreenModel: SettingsScreenModel
    override val hapticsService: HapticsService
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

    @Provides fun provideApplicationContext(application: Application): Context = application

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides application: Application,
        ): AndroidAppGraph
    }

    @Provides
    fun provideHapticsService(impl: AndroidHapticsServiceImpl): HapticsService = impl

    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        val dbFile = context.getDatabasePath("memory_match.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6
        )
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

fun createAndroidGraph(application: Application): AppGraph = createGraphFactory<AndroidAppGraph.Factory>().create(application)
