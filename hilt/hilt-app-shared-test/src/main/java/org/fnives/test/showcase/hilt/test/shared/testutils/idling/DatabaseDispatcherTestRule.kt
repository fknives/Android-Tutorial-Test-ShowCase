package org.fnives.test.showcase.hilt.test.shared.testutils.idling

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.anyResourceNotIdle
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.awaitIdlingResources
import org.fnives.test.showcase.android.testutil.synchronization.runOnUIAwaitOnCurrent
import org.fnives.test.showcase.hilt.test.shared.testutils.storage.TestDatabaseInitialization
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseDispatcherTestRule : TestRule {

    lateinit var testDispatcher: TestDispatcher

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val dispatcher = StandardTestDispatcher()
                testDispatcher = dispatcher
                TestDatabaseInitialization.dispatcher = dispatcher
                base.evaluate()
            }
        }

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
            while (anyResourceNotIdle()) { // check if any request is in progress
                awaitIdlingResources() // complete all requests and other idling resources
                scheduler.advanceUntilIdle() // run coroutines after request is finished
            }
            scheduler.advanceUntilIdle()
        }
    }
}
