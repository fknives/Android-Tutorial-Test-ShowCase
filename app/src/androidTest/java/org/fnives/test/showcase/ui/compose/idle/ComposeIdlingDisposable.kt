package org.fnives.test.showcase.ui.compose.idle

import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.junit4.ComposeTestRule
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.Disposable

class ComposeIdlingDisposable(
    private val idlingResource: IdlingResource,
    private val testRule: ComposeTestRule,
) : Disposable {
    override var isDisposed: Boolean = false
        private set

    init {
        testRule.registerIdlingResource(idlingResource)
    }

    override fun dispose() {
        isDisposed = true
        testRule.unregisterIdlingResource(idlingResource)
    }
}
