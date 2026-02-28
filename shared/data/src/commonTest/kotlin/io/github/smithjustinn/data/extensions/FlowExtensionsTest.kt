package io.github.smithjustinn.data.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FlowExtensionsTest {
    @Test
    fun testMapToStateFlowEagerly() =
        runTest(UnconfinedTestDispatcher()) {
            val sourceFlow = MutableSharedFlow<Int>()
            val stateFlow =
                sourceFlow.mapToStateFlow(
                    scope = backgroundScope,
                    initialValue = "0",
                    started = SharingStarted.Eagerly,
                ) {
                    it.toString()
                }

            // Check initial value
            assertEquals("0", stateFlow.value)

            // Emit and check new value
            sourceFlow.emit(1)
            assertEquals("1", stateFlow.value)

            // Emit and check another value
            sourceFlow.emit(2)
            assertEquals("2", stateFlow.value)
        }

    @Test
    fun testMapToStateFlowLazily() =
        runTest(UnconfinedTestDispatcher()) {
            val sourceFlow = MutableSharedFlow<Int>(replay = 1)
            sourceFlow.tryEmit(1)

            val stateFlow =
                sourceFlow.mapToStateFlow(
                    scope = backgroundScope,
                    initialValue = "0",
                    started = SharingStarted.Lazily,
                ) {
                    it.toString()
                }

            // Before collection, lazily started flow should have initial value
            assertEquals("0", stateFlow.value)

            // Launch a collector to start the sharing
            val job =
                launch {
                    stateFlow.collect {}
                }

            // Value should update to the emitted value after sharing starts
            assertEquals("1", stateFlow.value)

            sourceFlow.emit(2)
            assertEquals("2", stateFlow.value)

            job.cancel()
        }

    @Test
    fun testMapToStateFlowTransform() =
        runTest(UnconfinedTestDispatcher()) {
            val sourceFlow = MutableSharedFlow<String>()
            val stateFlow =
                sourceFlow.mapToStateFlow(
                    scope = backgroundScope,
                    initialValue = 0,
                    started = SharingStarted.Eagerly,
                ) {
                    it.length
                }

            assertEquals(0, stateFlow.value)

            sourceFlow.emit("hello")
            assertEquals(5, stateFlow.value)

            sourceFlow.emit("world!")
            assertEquals(6, stateFlow.value)
        }

    @Test
    fun testMapToStateFlowWithFlowOf() =
        runTest(UnconfinedTestDispatcher()) {
            val sourceFlow = flowOf(1, 2, 3)
            val stateFlow =
                sourceFlow.mapToStateFlow(
                    scope = backgroundScope,
                    initialValue = "0",
                    started = SharingStarted.Eagerly,
                ) {
                    it.toString()
                }

            // Since it's eagerly started and flowOf emits immediately,
            // it should eventually settle on the last value.
            // We use UnconfinedTestDispatcher, so it might be immediate.
            // Let's collect the values.
            val values = mutableListOf<String>()
            val job =
                launch {
                    stateFlow.toList(values)
                }

            // Wait for flowOf to complete emission
            // In UnconfinedTestDispatcher this is immediate, but stateFlow conflates.
            // It might settle on "3"
            assertEquals("3", stateFlow.value)
            job.cancel()
        }
}
