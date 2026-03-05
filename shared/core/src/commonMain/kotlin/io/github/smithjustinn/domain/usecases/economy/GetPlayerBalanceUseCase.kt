package io.github.smithjustinn.domain.usecases.economy

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import kotlinx.coroutines.flow.StateFlow

@Inject
class GetPlayerBalanceUseCase(
    private val repository: PlayerEconomyRepository,
) {
    operator fun invoke(): StateFlow<Long> = repository.balance
}
