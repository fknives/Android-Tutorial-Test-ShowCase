package org.fnives.test.showcase.android.testutil

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Junit5 Version of InstantTaskExecutorRule from Junit4
 *
 * reference: https://developer.android.com/reference/androidx/arch/core/executor/testing/InstantTaskExecutorRule
 *
 * A JUnit5 Extensions that swaps the background executor used by the Architecture Components with a different
 * one which executes each task synchronously.
 * You can use this extension for your host side tests that use Architecture Components.
 */
@SuppressLint("RestrictedApi")
class InstantExecutorExtension : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance()
            .setDelegate(object : TaskExecutor() {
                override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

                override fun postToMainThread(runnable: Runnable) = runnable.run()

                override fun isMainThread(): Boolean = true
            })
    }

    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}
