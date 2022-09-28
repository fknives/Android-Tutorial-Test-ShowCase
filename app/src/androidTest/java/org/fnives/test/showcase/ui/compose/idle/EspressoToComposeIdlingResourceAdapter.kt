package org.fnives.test.showcase.ui.compose.idle

import androidx.test.espresso.IdlingResource

class EspressoToComposeIdlingResourceAdapter(private val idlingResource: IdlingResource) : androidx.compose.ui.test.IdlingResource {
    override val isIdleNow: Boolean get() = idlingResource.isIdleNow
}
