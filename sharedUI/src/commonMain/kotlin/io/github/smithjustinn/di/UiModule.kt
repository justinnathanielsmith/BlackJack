package io.github.smithjustinn.di

import io.github.smithjustinn.domain.usecases.economy.BuyItemUseCase
import io.github.smithjustinn.domain.usecases.economy.EarnCurrencyUseCase
import io.github.smithjustinn.domain.usecases.economy.GetPlayerBalanceUseCase
import io.github.smithjustinn.domain.usecases.economy.GetShopItemsUseCase
import io.github.smithjustinn.domain.usecases.game.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.game.ClearSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.game.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.game.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.game.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val uiModule =
    module {
        singleOf(::CalculateFinalScoreUseCase)
        singleOf(::ClearSavedGameUseCase)
        singleOf(::FlipCardUseCase)
        singleOf(::GetSavedGameUseCase)
        singleOf(::ResetErrorCardsUseCase)
        singleOf(::SaveGameStateUseCase)
        singleOf(::StartNewGameUseCase)
        singleOf(::GetGameStatsUseCase)
        singleOf(::SaveGameResultUseCase)
        singleOf(::EarnCurrencyUseCase)
        singleOf(::BuyItemUseCase)
        singleOf(::GetPlayerBalanceUseCase)
        singleOf(::GetShopItemsUseCase)
    }
