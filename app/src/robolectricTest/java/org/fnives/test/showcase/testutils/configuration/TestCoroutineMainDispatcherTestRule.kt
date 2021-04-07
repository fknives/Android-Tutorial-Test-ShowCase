package org.fnives.test.showcase.testutils.configuration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.fnives.test.showcase.testutils.idling.advanceUntilIdleWithIdlingResources
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestCoroutineMainDispatcherTestRule : MainDispatcherTestRule {

    private lateinit var testDispatcher: TestCoroutineDispatcher

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val dispatcher = TestCoroutineDispatcher()
                dispatcher.pauseDispatcher()
                Dispatchers.setMain(dispatcher)
                testDispatcher = dispatcher
                DatabaseInitialization.dispatcher = dispatcher
                try {
                    base.evaluate()
                } finally {
                    Dispatchers.resetMain()
                }
            }
        }

    override fun advanceUntilIdleWithIdlingResources() {
        testDispatcher.advanceUntilIdleWithIdlingResources()
    }

    override fun advanceUntilIdleOrActivityIsDestroyed() {
        advanceUntilIdleWithIdlingResources()
    }

    override fun advanceUntilIdle() {
        testDispatcher.advanceUntilIdle()
    }

    override fun advanceTimeBy(delayInMillis: Long) {
        testDispatcher.advanceTimeBy(delayInMillis)
    }
}
