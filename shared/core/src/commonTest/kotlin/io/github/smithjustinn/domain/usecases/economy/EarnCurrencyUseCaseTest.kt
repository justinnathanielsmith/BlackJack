package io.github.smithjustinn.domain.usecases.economy

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EarnCurrencyUseCaseTest {

    private val repository = mock<PlayerEconomyRepository>()
    private val useCase = DefaultEarnCurrencyUseCase(repository)

    @Test
    fun `execute should add currency when amount is positive`() = runTest {
        val amount = 100L
        everySuspend { repository.addCurrency(amount) } returns Unit
        useCase.execute(amount)
        verifySuspend { repository.addCurrency(amount) }
    }

    @Test
    fun `execute should not add currency when amount is zero`() = runTest {
        val amount = 0L
        useCase.execute(amount)
        verifySuspend(VerifyMode.exactly(0)) { repository.addCurrency(amount) }
    }

    @Test
    fun `execute should not add currency when amount is negative`() = runTest {
        val amount = -50L
        useCase.execute(amount)
        verifySuspend(VerifyMode.exactly(0)) { repository.addCurrency(amount) }
    }
}
