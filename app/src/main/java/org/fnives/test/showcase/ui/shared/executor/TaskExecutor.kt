package org.fnives.test.showcase.ui.shared.executor

/**
 * Define TaskExecutor intended for [AsyncDifferConfig][androidx.recyclerview.widget.AsyncDifferConfig]
 */
interface TaskExecutor {
    fun executeOnDiskIO(runnable: Runnable)

    fun postToMainThread(runnable: Runnable)
}
