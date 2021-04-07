package org.fnives.test.showcase.testutils.idling

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.matcher.ViewMatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.fnives.test.showcase.testutils.viewactions.LoopMainThreadFor
import org.fnives.test.showcase.testutils.viewactions.LoopMainThreadUntilIdle

private val idleScope = CoroutineScope(Dispatchers.IO)

// workaround, issue with idlingResources is tracked here https://github.com/robolectric/robolectric/issues/4807
fun anyResourceIdling(): Boolean = !IdlingRegistry.getInstance().resources.all(IdlingResource::isIdleNow)

fun awaitIdlingResources() {
    val idlingRegistry = IdlingRegistry.getInstance()
    if (idlingRegistry.resources.all(IdlingResource::isIdleNow)) return

    var isIdle = false
    idleScope.launch {
        do {
            idlingRegistry.resources
                .filterNot(IdlingResource::isIdleNow)
                .forEach { idlingRegistry ->
                    idlingRegistry.awaitUntilIdle()
                }
        } while (!idlingRegistry.resources.all(IdlingResource::isIdleNow))
        isIdle = true
    }
    while (!isIdle) {
        loopMainThreadFor(200L)
    }
}

private suspend fun IdlingResource.awaitUntilIdle() {
    // using loop because some times, registerIdleTransitionCallback wasn't called
    while (true) {
        if (isIdleNow) return
        delay(100)
    }
}

fun TestCoroutineDispatcher.advanceUntilIdleWithIdlingResources() {
    advanceUntilIdle() // advance until a request is sent
    while (anyResourceIdling()) { // check if any request is in progress
        awaitIdlingResources() // complete all requests and other idling resources
        advanceUntilIdle() // run coroutines after request is finished
    }
    advanceUntilIdle()
}

fun loopMainThreadUntilIdleWithIdlingResources() {
    Espresso.onView(ViewMatchers.isRoot()).perform(LoopMainThreadUntilIdle()) // advance until a request is sent
    while (anyResourceIdling()) { // check if any request is in progress
        awaitIdlingResources() // complete all requests and other idling resources
        Espresso.onView(ViewMatchers.isRoot()).perform(LoopMainThreadUntilIdle()) // run coroutines after request is finished
    }
    Espresso.onView(ViewMatchers.isRoot()).perform(LoopMainThreadUntilIdle())
}

fun loopMainThreadFor(delay: Long) {
    Espresso.onView(ViewMatchers.isRoot()).perform(LoopMainThreadFor(delay))
}
