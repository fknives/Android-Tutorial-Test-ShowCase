package org.fnives.test.showcase.testutils.viewactions

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ProgressBar
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

class ReplaceProgressBarDrawableToStatic : ViewAction {
    override fun getConstraints(): Matcher<View> =
        isAssignableFrom(ProgressBar::class.java)

    override fun getDescription(): String =
        "replace the ProgressBar drawable"

    override fun perform(uiController: UiController, view: View) {
        val progressBar: ProgressBar = view as ProgressBar
        progressBar.indeterminateDrawable = ColorDrawable(Color.GREEN)
        uiController.loopMainThreadUntilIdle()
    }
}