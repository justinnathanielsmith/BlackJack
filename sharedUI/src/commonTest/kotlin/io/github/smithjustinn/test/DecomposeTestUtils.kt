package io.github.smithjustinn.test

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A utility for running Decompose component tests with proper lifecycle management. It
 * automatically creates, starts, and destroys a [LifecycleRegistry].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runComponentTest(
    testDispatcher: TestDispatcher,
    timeout: Duration = 10.seconds,
    block: suspend TestScope.(lifecycle: LifecycleRegistry) -> Unit,
) =
    runTest(testDispatcher, timeout = timeout) {
        val lifecycle = LifecycleRegistry()
        lifecycle.onCreate()
        lifecycle.onStart()
        lifecycle.onResume()
        try {
            block(lifecycle)
        } finally {
            lifecycle.onPause()
            lifecycle.onStop()
            lifecycle.onDestroy()
        }
    }

/** A helper to handle coroutine dispatcher setup and cleanup in tests. */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestHelper(val testDispatcher: TestDispatcher) {
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    fun tearDown() {
        Dispatchers.resetMain()
    }
}
