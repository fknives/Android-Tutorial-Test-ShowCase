package org.fnives.test.showcase.android.testutil.viewaction.recycler

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

/**
 * Sets the [RecyclerView]'s [itemAnimator][RecyclerView.setItemAnimator] to null, thus disabling animations.
 */
class RemoveItemAnimations : ViewAction {
    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isAssignableFrom(RecyclerView::class.java)

    override fun getDescription(): String =
        "Remove item animations"

    override fun perform(uiController: UiController, view: View) {
        val recycler: RecyclerView = view as RecyclerView
        recycler.itemAnimator = null
        uiController.loopMainThreadUntilIdle()
    }
}
