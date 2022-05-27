package org.fnives.test.showcase.android.testutil.viewaction

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class LoopMainThreadUntilIdle : ViewAction {
    override fun getConstraints(): Matcher<View> = Matchers.isA(View::class.java)

    override fun getDescription(): String = "loop MainThread for until Idle"

    override fun perform(uiController: UiController, view: View?) {
        uiController.loopMainThreadUntilIdle()
    }
}