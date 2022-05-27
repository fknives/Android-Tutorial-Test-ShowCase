package org.fnives.test.showcase.android.testutil.synchronization

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

/**
 * Runs the given action on the MainThread and blocks currentThread, until it is completed.
 *
 * It is safe to call this from the MainThread.
 */
fun runOnUIAwaitOnCurrent(action: () -> Unit) {
    if (Looper.myLooper() === Looper.getMainLooper()) {
        action()
    } else {
        val deferred = CompletableDeferred<Unit>()
        Handler(Looper.getMainLooper()).post {
            action()
            deferred.complete(Unit)
        }
        runBlocking { deferred.await() }
    }
}
