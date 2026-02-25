package io.github.smithjustinn.test

import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.runTest as runCoroutineTest

/**
 * An abstract base class for core logic tests that provides standardized coroutine setup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseLogicTest {
    protected val testDispatcher = StandardTestDispatcher()
    protected var testScope = TestScope(testDispatcher)
        private set

    protected val testDispatchers =
        CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )

    @BeforeTest
    open fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
    }

    @AfterTest
    open fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    /**
     * A helper to run logic tests.
     */
    protected fun runTest(testBody: suspend TestScope.() -> Unit) = testScope.runCoroutineTest(testBody = testBody)
}
