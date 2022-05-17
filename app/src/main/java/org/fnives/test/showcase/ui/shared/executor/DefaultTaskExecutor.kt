package org.fnives.test.showcase.ui.shared.executor

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

/**
 * Basic copy of [androidx.arch.core.executor.DefaultTaskExecutor], needed because that is restricted to Library.
 * With a Flavour of [androidx.recyclerview.widget.AsyncDifferConfig].
 * Used within [AsyncTaskExecutor].
 *
 * Intended to be used for AsyncDiffUtil so it can be synchronized with Espresso.
 */
class DefaultTaskExecutor : TaskExecutor {

    private val diskIO = Executors.newFixedThreadPool(2)
    private val mMainHandler: Handler by lazy { createAsync(Looper.getMainLooper()) }

    override fun executeOnDiskIO(runnable: Runnable) {
        diskIO.execute(runnable)
    }

    override fun postToMainThread(runnable: Runnable) {
        mMainHandler.post(runnable)
    }

    private fun createAsync(looper: Looper): Handler =
        if (Build.VERSION.SDK_INT >= 28) {
            Handler.createAsync(looper)
        } else {
            Handler(looper)
        }
}
