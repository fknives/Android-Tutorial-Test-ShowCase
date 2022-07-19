package org.fnives.test.showcase.android.testutil.synchronization

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.anyResourceNotIdle
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.awaitIdlingResources
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@OptIn(ExperimentalCoroutinesApi::class)
open class MainDispatcherTestRule(private val useStandard: Boolean = true) : TestRule {

    private lateinit var testDispatcher: TestDispatcher

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val dispatcher = if (useStandard) StandardTestDispatcher() else UnconfinedTestDispatcher()
                Dispatchers.setMain(dispatcher)
                testDispatcher = dispatcher
                onTestDispatcherInitialized(testDispatcher)
                try {
                    base.evaluate()
                } finally {
                    Dispatchers.resetMain()
                    onTestDispatcherReset()
                }
            }
        }

    open fun onTestDispatcherInitialized(testDispatcher: TestDispatcher) = Unit

    open fun onTestDispatcherReset() = Unit

    fun advanceUntilIdleWithIdlingResources() {
        System.err.println("calling")
        runOnUIAwaitOnCurrent {
            System.err.println("calling runonUIAwait")
            testDispatcher.advanceUntilIdleWithIdlingResources()
        }
    }

    fun advanceUntilIdle() = runOnUIAwaitOnCurrent {
        testDispatcher.scheduler.advanceUntilIdle()
    }

    fun advanceTimeBy(delayInMillis: Long) = runOnUIAwaitOnCurrent {
        testDispatcher.scheduler.advanceTimeBy(delayInMillis)
    }

    companion object {
        fun TestDispatcher.advanceUntilIdleWithIdlingResources() {
            System.err.println("test - before advanced1")
            scheduler.advanceUntilIdle() // advance until a request is sent
            System.err.println("test - advanced1")
            while (anyResourceNotIdle()) { // check if any request is in progress
                awaitIdlingResources() // complete all requests and other idling resources
                System.err.println("test - awaited idling")
                scheduler.advanceUntilIdle() // run coroutines after request is finished
                System.err.println("test - advanced 2")
            }
            scheduler.advanceUntilIdle()
            System.err.println("test - advanced last")
        }
    }
}
