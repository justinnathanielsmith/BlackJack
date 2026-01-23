package io.github.smithjustinn.di.modules

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.data.local.SettingsDao
import io.github.smithjustinn.data.repositories.GameStateRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import io.github.smithjustinn.data.repositories.SettingsRepositoryImpl
import io.github.smithjustinn.di.AppScope
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.serialization.json.Json

@BindingContainer
object DataModule {
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

    @Provides
    @SingleIn(AppScope::class)
    fun provideDispatchers(): io.github.smithjustinn.utils.CoroutineDispatchers = io.github.smithjustinn.utils.CoroutineDispatchers()
}
