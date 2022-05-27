package org.fnives.test.showcase.android.testutil.synchronization

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.anyResourceIdling
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

    fun advanceUntilIdleWithIdlingResources() = runOnUIAwaitOnCurrent {
        testDispatcher.advanceUntilIdleWithIdlingResources()
    }

    fun advanceUntilIdle() = runOnUIAwaitOnCurrent {
        testDispatcher.scheduler.advanceUntilIdle()
    }

    fun advanceTimeBy(delayInMillis: Long) = runOnUIAwaitOnCurrent {
        testDispatcher.scheduler.advanceTimeBy(delayInMillis)
    }

    companion object {
        fun TestDispatcher.advanceUntilIdleWithIdlingResources() {
            scheduler.advanceUntilIdle() // advance until a request is sent
            while (anyResourceIdling()) { // check if any request is in progress
                awaitIdlingResources() // complete all requests and other idling resources
                scheduler.advanceUntilIdle() // run coroutines after request is finished
            }
            scheduler.advanceUntilIdle()
        }
    }
}
