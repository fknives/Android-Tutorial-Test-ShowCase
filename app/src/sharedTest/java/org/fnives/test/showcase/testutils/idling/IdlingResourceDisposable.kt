package org.fnives.test.showcase.testutils.idling

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource

internal class IdlingResourceDisposable(private val idlingResource: IdlingResource) : Disposable {
    override var isDisposed: Boolean = false
        private set

    init {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    override fun dispose() {
        if (isDisposed) return
        isDisposed = true
        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}
