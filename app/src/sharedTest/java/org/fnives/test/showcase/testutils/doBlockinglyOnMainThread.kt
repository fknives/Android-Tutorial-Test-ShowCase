package org.fnives.test.showcase.testutils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

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
