package io.github.smithjustinn.test

import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    protected val testScope = TestScope(testDispatcher)

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
    }

    @AfterTest
    open fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * A helper to run logic tests using the class-level [testDispatcher].
     */
    protected fun runTest(testBody: suspend TestScope.() -> Unit) =
        runCoroutineTest(context = testDispatcher, testBody = testBody)
}
