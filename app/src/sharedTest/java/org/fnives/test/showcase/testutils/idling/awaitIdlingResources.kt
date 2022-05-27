package org.fnives.test.showcase.testutils.idling

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.fnives.test.showcase.android.testutil.synchronization.loopMainThreadFor
import java.util.concurrent.Executors

// workaround, issue with idlingResources is tracked here https://github.com/robolectric/robolectric/issues/4807
fun anyResourceIdling(): Boolean = !IdlingRegistry.getInstance().resources.all(IdlingResource::isIdleNow)

fun awaitIdlingResources() {
    val idlingRegistry = IdlingRegistry.getInstance()
    if (idlingRegistry.resources.all(IdlingResource::isIdleNow)) return

    val executor = Executors.newSingleThreadExecutor()
    var isIdle = false
    executor.submit {
        do {
            idlingRegistry.resources
                .filterNot(IdlingResource::isIdleNow)
                .forEach { idlingResource ->
                    idlingResource.awaitUntilIdle()
                }
        } while (!idlingRegistry.resources.all(IdlingResource::isIdleNow))
        isIdle = true
    }
    while (!isIdle) {
        loopMainThreadFor(200L)
    }
    executor.shutdown()
}

private fun IdlingResource.awaitUntilIdle() {
    // using loop because some times, registerIdleTransitionCallback wasn't called
    while (true) {
        if (isIdleNow) return
        Thread.sleep(100L)
    }
}
