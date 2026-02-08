package io.github.smithjustinn.ui.debug

import com.arkivanov.decompose.ComponentContext
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.services.AdService
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.ad_unit_id
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

interface DebugComponent {
    val state: StateFlow<DebugState>

    fun onBackClicked()

    fun onShowRewardedAdClicked()

    fun onAddCurrencyClicked(amount: Long)

    fun onResetCurrencyClicked()
}

sealed class AdStatus {
    data object Idle : AdStatus()

    data object Loading : AdStatus()

    data object Showing : AdStatus()

    data class RewardEarned(
        val amount: Int,
    ) : AdStatus()

    data class Error(
        val message: String,
    ) : AdStatus()
}

data class DebugState(
    val balance: Long = 0,
    val adStatus: AdStatus = AdStatus.Idle,
)

class DefaultDebugComponent(
    componentContext: ComponentContext,
    private val appGraph: AppGraph,
    private val onBackClicked: () -> Unit,
) : DebugComponent,
    ComponentContext by componentContext {
    private val _state = MutableStateFlow(DebugState())
    override val state: StateFlow<DebugState> = _state.asStateFlow()

    private val economyRepository: PlayerEconomyRepository = appGraph.playerEconomyRepository
    private val adService: AdService = appGraph.adService

    // Use lifecycle.componentScope extension
    private val scope = lifecycle.componentScope(appGraph.coroutineDispatchers.mainImmediate)

    init {
        economyRepository.balance
            .onEach { balance ->
                _state.update { it.copy(balance = balance) }
            }.launchIn(scope)
    }

    override fun onBackClicked() {
        onBackClicked.invoke()
    }

    @Suppress("TooGenericExceptionCaught")
    override fun onShowRewardedAdClicked() {
        scope.launch {
            _state.update { it.copy(adStatus = AdStatus.Loading) }
            val adUnitId = getString(Res.string.ad_unit_id)

            try {
                adService.loadRewardedAd(adUnitId)
                _state.update { it.copy(adStatus = AdStatus.Showing) }

                adService.showRewardedAd { rewardItem ->
                    _state.update { it.copy(adStatus = AdStatus.RewardEarned(rewardItem)) }
                    launch {
                        economyRepository.addCurrency(rewardItem.toLong())
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(adStatus = AdStatus.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    override fun onAddCurrencyClicked(amount: Long) {
        scope.launch {
            economyRepository.addCurrency(amount)
        }
    }

    override fun onResetCurrencyClicked() {
        scope.launch {
            val current = _state.value.balance
            if (current > 0) {
                economyRepository.deductCurrency(current)
            }
        }
    }
}
