package org.fnives.test.showcase.android.testutil.viewaction

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class LoopMainThreadFor(private val delayInMillis: Long) : ViewAction {
    override fun getConstraints(): Matcher<View> = Matchers.isA(View::class.java)

    override fun getDescription(): String = "loop MainThread for $delayInMillis milliseconds"

    override fun perform(uiController: UiController, view: View?) {
        uiController.loopMainThreadForAtLeast(delayInMillis)
    }
}
