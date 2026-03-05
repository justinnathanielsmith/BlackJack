package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.di.AppScope

interface EarnCurrencyUseCase {
    suspend fun execute(amount: Long)
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultEarnCurrencyUseCase(
    private val repository: PlayerEconomyRepository,
) : EarnCurrencyUseCase {
    override suspend fun execute(amount: Long) {
        if (amount > 0) {
            repository.addCurrency(amount)
        }
    }
}
