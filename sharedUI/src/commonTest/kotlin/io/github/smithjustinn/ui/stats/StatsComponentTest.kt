package io.github.smithjustinn.ui.stats

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StatsComponentTest {

    private val leaderboardRepository: LeaderboardRepository = mock()
    private val appGraph: AppGraph = mock()
    
    private lateinit var component: DefaultStatsComponent
    private val testDispatcher = StandardTestDispatcher()
    private var lifecycle: LifecycleRegistry? = null

    private fun runStatsTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        val l = LifecycleRegistry()
        l.onCreate()
        lifecycle = l
        try {
            block()
        } finally {
            l.onDestroy()
            lifecycle = null
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { appGraph.leaderboardRepository } returns leaderboardRepository
        every { appGraph.logger } returns co.touchlab.kermit.Logger(co.touchlab.kermit.StaticConfig())
        
        // Mock empty leaderboard flows for all difficulties
        // This is a bit tedious because we need many mocks
        val difficulties = io.github.smithjustinn.domain.models.DifficultyLevel.defaultLevels
        difficulties.forEach { level ->
            every { leaderboardRepository.getTopEntries(level.pairs, GameMode.STANDARD) } returns MutableStateFlow(emptyList())
            every { leaderboardRepository.getTopEntries(level.pairs, GameMode.TIME_ATTACK) } returns MutableStateFlow(emptyList())
        }

        every { appGraph.coroutineDispatchers } returns CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        lifecycle?.onDestroy()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runStatsTest {
        component = createComponent()
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            val state = awaitItem()
            assertEquals(GameMode.STANDARD, state.selectedGameMode)
            assertEquals(io.github.smithjustinn.domain.models.DifficultyLevel.defaultLevels.size, state.difficultyLeaderboards.size)
        }
    }

    @Test
    fun `onGameModeSelected updates state`() = runStatsTest {
        component = createComponent()
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            awaitItem() // Initial state
            component.onGameModeSelected(GameMode.TIME_ATTACK)
            val newState = awaitItem()
            assertEquals(GameMode.TIME_ATTACK, newState.selectedGameMode)
        }
    }

    private fun createComponent(): DefaultStatsComponent {
        return DefaultStatsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle!!),
            appGraph = appGraph,
            onBackClicked = {}
        )
    }
}
