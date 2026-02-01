package io.github.smithjustinn.di

import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.repositories.DailyChallengeRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStateRepositoryImpl
import io.github.smithjustinn.data.repositories.GameStatsRepositoryImpl
import io.github.smithjustinn.data.repositories.LeaderboardRepositoryImpl
import io.github.smithjustinn.data.repositories.PlayerEconomyRepositoryImpl
import io.github.smithjustinn.data.repositories.SettingsRepositoryImpl
import io.github.smithjustinn.domain.repositories.DailyChallengeRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
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
        single { get<AppDatabase>().playerEconomyDao() }

        singleOf(::GameStatsRepositoryImpl) { bind<GameStatsRepository>() }
        singleOf(::LeaderboardRepositoryImpl) { bind<LeaderboardRepository>() }
        singleOf(::GameStateRepositoryImpl) { bind<GameStateRepository>() }
        singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
        singleOf(::DailyChallengeRepositoryImpl) { bind<DailyChallengeRepository>() }
        singleOf(::PlayerEconomyRepositoryImpl) { bind<PlayerEconomyRepository>() }

        single { Json { ignoreUnknownKeys = true } }
    }
