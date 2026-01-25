package io.github.smithjustinn.di.modules

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.repositories.GameStateRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import io.github.smithjustinn.data.repositories.SettingsRepositoryImpl
import io.github.smithjustinn.data.repository.DailyChallengeRepository
import io.github.smithjustinn.data.repository.DailyChallengeRepositoryImpl
import io.github.smithjustinn.di.AppScope
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.serialization.json.Json

@BindingContainer
object RepositoryModule {
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
    fun provideDailyChallengeRepository(impl: DailyChallengeRepositoryImpl): DailyChallengeRepository = impl

    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @SingleIn(AppScope::class)
    fun provideDispatchers(): CoroutineDispatchers = CoroutineDispatchers()
}
