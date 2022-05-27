package org.fnives.test.showcase.testutils.idling

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.anyResourceIdling
import org.fnives.test.showcase.android.testutil.synchronization.runOnUIAwaitOnCurrent
import org.fnives.test.showcase.testutils.storage.TestDatabaseInitialization
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.awaitIdlingResources

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherTestRule : TestRule {

    private lateinit var testDispatcher: TestDispatcher

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val dispatcher = StandardTestDispatcher()
                Dispatchers.setMain(dispatcher)
                testDispatcher = dispatcher
                TestDatabaseInitialization.overwriteDatabaseInitialization(dispatcher)
                try {
                    base.evaluate()
                } finally {
                    Dispatchers.resetMain()
                }
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
            while (anyResourceIdling()) { // check if any request is in progress
                awaitIdlingResources() // complete all requests and other idling resources
                scheduler.advanceUntilIdle() // run coroutines after request is finished
            }
            scheduler.advanceUntilIdle()
        }
    }
}
