package org.fnives.test.showcase.android.testutil.synchronization

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.android.testutil.viewaction.LoopMainThreadFor

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

fun loopMainThreadFor(delay: Long) {
    if (Looper.getMainLooper().thread == Thread.currentThread()) {
        Thread.sleep(200L)
    } else {
        Espresso.onView(ViewMatchers.isRoot()).perform(LoopMainThreadFor(delay))
    }
}
