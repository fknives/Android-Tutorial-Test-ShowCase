package org.fnives.test.showcase.hilt.test.shared.testutils.idling

import org.fnives.test.showcase.hilt.ui.shared.executor.AsyncTaskExecutor
import org.fnives.test.showcase.hilt.ui.shared.executor.TaskExecutor
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Similar Test Rule to InstantTaskExecutorRule just for the [AsyncTaskExecutor] to make AsyncDiffUtil synchronized.
 */
class AsyncDiffUtilInstantTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                AsyncTaskExecutor.delegate = object : TaskExecutor {
                    override fun executeOnDiskIO(runnable: Runnable) {
                        runnable.run()
                    }

                    override fun postToMainThread(runnable: Runnable) {
                        runnable.run()
                    }
                }

                base.evaluate()

                AsyncTaskExecutor.delegate = null
            }
        }
}
