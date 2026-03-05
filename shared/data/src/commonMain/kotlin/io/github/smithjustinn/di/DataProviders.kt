package io.github.smithjustinn.di

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.DailyChallengeDao
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.data.local.PlayerEconomyDao
import io.github.smithjustinn.data.local.SettingsDao
import kotlinx.serialization.json.Json

/**
 * Provides data-layer dependencies (DAOs from AppDatabase, Json) to the Metro dependency graph.
 * Replaces the old Koin dataModule.
 */
@ContributesTo(AppScope::class)
interface DataProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideGameStatsDao(database: AppDatabase): GameStatsDao = database.gameStatsDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideLeaderboardDao(database: AppDatabase): LeaderboardDao = database.leaderboardDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideGameStateDao(database: AppDatabase): GameStateDao = database.gameStateDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideDailyChallengeDao(database: AppDatabase): DailyChallengeDao = database.dailyChallengeDao()

    @SingleIn(AppScope::class)
    @Provides
    fun providePlayerEconomyDao(database: AppDatabase): PlayerEconomyDao = database.playerEconomyDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}
