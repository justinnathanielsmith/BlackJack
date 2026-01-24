package io.github.smithjustinn.ui.settings

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.test.runComponentTest
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsComponentTest {

    private val settingsRepository: SettingsRepository = mock()
    private val appGraph: AppGraph = mock()
    
    private lateinit var component: DefaultSettingsComponent
    private val testDispatcher = StandardTestDispatcher()


    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { appGraph.settingsRepository } returns settingsRepository
        every { settingsRepository.isPeekEnabled } returns MutableStateFlow(true)
        every { settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(true)
        every { settingsRepository.isSoundEnabled } returns MutableStateFlow(true)
        every { settingsRepository.isMusicEnabled } returns MutableStateFlow(true)
        every { settingsRepository.soundVolume } returns MutableStateFlow(0.8f)
        every { settingsRepository.musicVolume } returns MutableStateFlow(0.5f)
        every { settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { settingsRepository.cardSymbolTheme } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { settingsRepository.areSuitsMultiColored } returns MutableStateFlow(false)

        everySuspend { settingsRepository.setPeekEnabled(any()) } returns Unit
        everySuspend { settingsRepository.setCardBackTheme(any()) } returns Unit
        everySuspend { settingsRepository.setMusicEnabled(any()) } returns Unit
        everySuspend { settingsRepository.setSoundEnabled(any()) } returns Unit
        everySuspend { settingsRepository.setSoundVolume(any()) } returns Unit
        everySuspend { settingsRepository.setMusicVolume(any()) } returns Unit

        every { appGraph.coroutineDispatchers } returns CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runComponentTest(testDispatcher) { lifecycle ->
        component = createComponent(lifecycle)
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            // The state might emit several times as flows are being collected
            // We want to find the first emission that matches our mocked configuration
            var foundDesiredState = false
            while (!foundDesiredState) {
                val state = awaitItem()
                if (state.soundVolume == 0.8f) {
                    assertTrue(state.isPeekEnabled)
                    assertEquals(CardBackTheme.GEOMETRIC, state.cardBackTheme)
                    foundDesiredState = true
                }
            }
        }
    }

    @Test
    fun `togglePeekEnabled updates repository`() = runComponentTest(testDispatcher) { lifecycle ->
        component = createComponent(lifecycle)
        testDispatcher.scheduler.runCurrent()

        component.togglePeekEnabled(false)
        testDispatcher.scheduler.runCurrent()

        verifySuspend { settingsRepository.setPeekEnabled(false) }
    }

    @Test
    fun `setCardBackTheme updates repository`() = runComponentTest(testDispatcher) { lifecycle ->
        component = createComponent(lifecycle)
        testDispatcher.scheduler.runCurrent()

        component.setCardBackTheme(CardBackTheme.GEOMETRIC)
        testDispatcher.scheduler.runCurrent()

        verifySuspend { settingsRepository.setCardBackTheme(CardBackTheme.GEOMETRIC) }
    }

    private fun createComponent(lifecycle: Lifecycle): DefaultSettingsComponent {
        return DefaultSettingsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph,
            onBackClicked = {}
        )
    }
}
