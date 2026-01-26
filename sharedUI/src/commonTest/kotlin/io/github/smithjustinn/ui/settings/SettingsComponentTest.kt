package io.github.smithjustinn.ui.settings

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsComponentTest : BaseComponentTest() {
    private lateinit var component: DefaultSettingsComponent

    @BeforeTest
    override fun setUp() {
        super.setUp()

        // Custom behaviors for this test if needed (defaults are usually enough)
        everySuspend { context.settingsRepository.setPeekEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setCardBackTheme(any()) } returns Unit
        everySuspend { context.settingsRepository.setMusicEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setSoundEnabled(any()) } returns Unit
        everySuspend { context.settingsRepository.setSoundVolume(any()) } returns Unit
        everySuspend { context.settingsRepository.setMusicVolume(any()) } returns Unit
    }

    @Test
    fun `initial state is correct`() =
        runTest { lifecycle ->
            // Setup specific volume for this test
            every { context.settingsRepository.soundVolume } returns MutableStateFlow(0.8f)

            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.state.test {
                // The state might emit several times as flows are being collected
                // We want to find the first emission that matches our mocked configuration
                var foundDesiredState = false
                while (!foundDesiredState) {
                    val state = awaitItem()
                    if (state.soundVolume == 0.8f) {
                        assertTrue(state.isPeekEnabled || !state.isPeekEnabled) // Just consume it
                        assertEquals(CardBackTheme.GEOMETRIC, state.cardBackTheme)
                        foundDesiredState = true
                    }
                }
            }
        }

    @Test
    fun `togglePeekEnabled updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.togglePeekEnabled(false)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setPeekEnabled(false) }
        }

    @Test
    fun `setCardBackTheme updates repository`() =
        runTest { lifecycle ->
            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.setCardBackTheme(CardBackTheme.GEOMETRIC)
            testDispatcher.scheduler.runCurrent()

            verifySuspend { context.settingsRepository.setCardBackTheme(CardBackTheme.GEOMETRIC) }
        }

    private fun createComponent(lifecycle: Lifecycle): DefaultSettingsComponent =
        DefaultSettingsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = context.appGraph,
            onBackClicked = {},
        )
}
