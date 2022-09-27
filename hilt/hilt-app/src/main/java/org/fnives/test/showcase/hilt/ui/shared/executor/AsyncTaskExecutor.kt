package org.fnives.test.showcase.hilt.ui.shared.executor

import java.util.concurrent.Executor

/**
 * Basic copy of [ArchTaskExecutor][androidx.arch.core.executor.ArchTaskExecutor], needed because that is restricted to Library.
 *
 * Intended to be used for [AsyncDifferConfig][androidx.recyclerview.widget.AsyncDifferConfig] so it can be synchronized with Espresso.
 *
 * Workaround until https://github.com/android/android-test/issues/382 is fixed finally.
 */
object AsyncTaskExecutor : TaskExecutor {

    val mainThreadExecutor = Executor { command -> postToMainThread(command) }
    val iOThreadExecutor = Executor { command -> executeOnDiskIO(command) }

    var delegate: TaskExecutor? = null
    private val defaultExecutor by lazy { DefaultTaskExecutor() }
    private val executor get() = delegate ?: defaultExecutor

    override fun executeOnDiskIO(runnable: Runnable) {
        executor.executeOnDiskIO(runnable)
    }

    override fun postToMainThread(runnable: Runnable) {
        executor.postToMainThread(runnable)
    }
}
