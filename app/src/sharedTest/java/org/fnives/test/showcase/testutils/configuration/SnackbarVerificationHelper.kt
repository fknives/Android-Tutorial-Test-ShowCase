package org.fnives.test.showcase.testutils.configuration

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class SnackbarVerificationHelper {

    fun assertIsShownWithText(@StringRes stringResID: Int, doDismiss: Boolean = true) {
        Espresso.onView(ViewMatchers.withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(stringResID)))
        if (doDismiss) {
            Espresso.onView(ViewMatchers.isAssignableFrom(Snackbar.SnackbarLayout::class.java)).perform(ViewActions.swipeRight())
            Espresso.onView(ViewMatchers.isRoot()).perform(LoopMainUntilSnackbarDismissed())
        }
    }

    fun assertIsNotShown() {
        Espresso.onView(ViewMatchers.withId(R.id.snackbar_text)).check(ViewAssertions.doesNotExist())
    }

    class LoopMainUntilSnackbarDismissed : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.isA(View::class.java)

        override fun getDescription(): String = "loop MainThread until Snackbar is Dismissed"

        override fun perform(uiController: UiController, view: View?) {
            while (view?.findViewById<View>(com.google.android.material.R.id.snackbar_text) != null) {
                uiController.loopMainThreadForAtLeast(100)
            }
        }
    }
}
