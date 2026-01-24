package io.github.smithjustinn.test

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

/**
 * An abstract base class for component tests that provides a [MokkeryTestContext] and handles
 * standardized coroutine setup and cleanup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseComponentTest {

    protected val testDispatcher = StandardTestDispatcher()
    protected val context = MokkeryTestContext(testDispatcher)
    protected val coroutineHelper = CoroutineTestHelper(testDispatcher)

    @BeforeTest
    open fun setUp() {
        coroutineHelper.setUp()
    }

    @AfterTest
    open fun tearDown() {
        coroutineHelper.tearDown()
    }

    /** A helper to run component tests using the class-level [testDispatcher]. */
    protected fun runTest(block: suspend TestScope.(LifecycleRegistry) -> Unit) =
            runComponentTest(testDispatcher, block = block)
}
