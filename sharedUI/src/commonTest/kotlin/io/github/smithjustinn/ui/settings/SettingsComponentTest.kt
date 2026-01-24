package io.github.smithjustinn.ui.settings

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsComponentTest {

    private val settingsRepository: SettingsRepository = mock()
    private val appGraph: AppGraph = mock()
    
    private lateinit var component: DefaultSettingsComponent
    private val testDispatcher = StandardTestDispatcher()
    private var lifecycle: LifecycleRegistry? = null

    private fun runSettingsTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
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
        lifecycle?.onDestroy()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runSettingsTest {
        component = createComponent()
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            // Skip initial default state
            val initial = awaitItem()
            if (initial.soundVolume == 1.0f) {
                val actual = awaitItem()
                assertTrue(actual.isPeekEnabled)
                assertEquals(0.8f, actual.soundVolume)
                assertEquals(CardBackTheme.GEOMETRIC, actual.cardBackTheme)
            } else {
                assertTrue(initial.isPeekEnabled)
                assertEquals(0.8f, initial.soundVolume)
            }
        }
    }

    @Test
    fun `togglePeekEnabled updates repository`() = runSettingsTest {
        component = createComponent()
        testDispatcher.scheduler.runCurrent()

        component.togglePeekEnabled(false)
        testDispatcher.scheduler.runCurrent()

        verifySuspend { settingsRepository.setPeekEnabled(false) }
    }

    @Test
    fun `setCardBackTheme updates repository`() = runSettingsTest {
        component = createComponent()
        testDispatcher.scheduler.runCurrent()

        component.setCardBackTheme(CardBackTheme.GEOMETRIC)
        testDispatcher.scheduler.runCurrent()

        verifySuspend { settingsRepository.setCardBackTheme(CardBackTheme.GEOMETRIC) }
    }

    private fun createComponent(): DefaultSettingsComponent {
        return DefaultSettingsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle!!),
            appGraph = appGraph,
            onBackClicked = {}
        )
    }
}
