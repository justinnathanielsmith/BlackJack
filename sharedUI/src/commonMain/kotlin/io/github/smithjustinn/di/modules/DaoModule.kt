package io.github.smithjustinn.di.modules

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.DailyChallengeDao
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.data.local.SettingsDao
import io.github.smithjustinn.di.AppScope

@BindingContainer
object DaoModule {
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
    fun provideDailyChallengeDao(database: AppDatabase): DailyChallengeDao =
        database.dailyChallengeDao()
}
