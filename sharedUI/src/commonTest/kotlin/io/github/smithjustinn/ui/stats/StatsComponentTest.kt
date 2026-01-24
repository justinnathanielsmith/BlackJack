package io.github.smithjustinn.ui.stats

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StatsComponentTest : BaseComponentTest() {

    private lateinit var component: DefaultStatsComponent

    private fun createComponent(lifecycle: Lifecycle): DefaultStatsComponent = DefaultStatsComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        appGraph = context.appGraph,
        onBackClicked = {},
    )

    @Test
    fun `initial state is correct`() = runTest { lifecycle ->
        component = createComponent(lifecycle)
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            val state = awaitItem()
            assertEquals(GameMode.STANDARD, state.selectedGameMode)
            assertEquals(
                io.github.smithjustinn.domain.models.DifficultyLevel.defaultLevels.size,
                state.difficultyLeaderboards.size,
            )
        }
    }

    @Test
    fun `onGameModeSelected updates state`() = runTest { lifecycle ->
        component = createComponent(lifecycle)
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            awaitItem() // Initial state
            component.onGameModeSelected(GameMode.TIME_ATTACK)
            val newState = awaitItem()
            assertEquals(GameMode.TIME_ATTACK, newState.selectedGameMode)
        }
    }
}
