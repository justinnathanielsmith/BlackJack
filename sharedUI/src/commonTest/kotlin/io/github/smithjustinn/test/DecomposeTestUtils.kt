package io.github.smithjustinn.test

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A utility for running Decompose component tests with proper lifecycle management.
 * It automatically creates, starts, and destroys a [LifecycleRegistry].
 */
fun runComponentTest(
    testDispatcher: TestDispatcher,
    timeout: Duration = 10.seconds,
    block: suspend TestScope.(lifecycle: LifecycleRegistry) -> Unit
) = runTest(testDispatcher, timeout = timeout) {
    val lifecycle = LifecycleRegistry()
    lifecycle.onCreate()
    try {
        block(lifecycle)
    } finally {
        lifecycle.onDestroy()
    }
}
