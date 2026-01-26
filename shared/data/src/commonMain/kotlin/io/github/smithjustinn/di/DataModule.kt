package io.github.smithjustinn.di

import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.repositories.GameStateRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import io.github.smithjustinn.data.repositories.SettingsRepositoryImpl
import io.github.smithjustinn.data.repository.DailyChallengeRepository
import io.github.smithjustinn.data.repository.DailyChallengeRepositoryImpl
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule =
    module {
        single { get<AppDatabase>().gameStatsDao() }
        single { get<AppDatabase>().leaderboardDao() }
        single { get<AppDatabase>().gameStateDao() }
        single { get<AppDatabase>().settingsDao() }
        single { get<AppDatabase>().dailyChallengeDao() }

        singleOf(::GameStatsRepositoryImpl) { bind<GameStatsRepository>() }
        singleOf(::LeaderboardRepositoryImpl) { bind<LeaderboardRepository>() }
        singleOf(::GameStateRepositoryImpl) { bind<GameStateRepository>() }
        singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
        singleOf(::DailyChallengeRepositoryImpl) { bind<DailyChallengeRepository>() }

        single { Json { ignoreUnknownKeys = true } }
    }
