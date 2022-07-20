package org.fnives.test.showcase.android.testutil.synchronization.idlingresources

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.fnives.test.showcase.android.testutil.synchronization.loopMainThreadFor
import java.util.concurrent.Executors

// workaround, issue with idlingResources is tracked here https://github.com/robolectric/robolectric/issues/4807
fun anyResourceNotIdle(): Boolean {
    val anyResourceNotIdle = (!IdlingRegistry.getInstance().resources.all(IdlingResource::isIdleNow))
    if (!anyResourceNotIdle) {
        // once it's idle we wait the Idling resource's time
        OkHttp3IdlingResource.sleepForDispatcherDefaultCallInRetrofitErrorState()
    }
    return anyResourceNotIdle
}

fun awaitIdlingResources() {
    if (!anyResourceNotIdle()) return
    val idlingRegistry = IdlingRegistry.getInstance()

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
        OkHttp3IdlingResource.sleepForDispatcherDefaultCallInRetrofitErrorState()
        isIdle = true
    }
    while (!isIdle) {
        loopMainThreadFor(200L)
    }
    executor.shutdown()
}

fun IdlingResource.awaitUntilIdle() {
    // using loop because some times, registerIdleTransitionCallback wasn't called
    while (true) {
        if (isIdleNow) return
        Thread.sleep(100L)
    }
}
