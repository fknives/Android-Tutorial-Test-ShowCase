package org.fnives.test.showcase.testutils.viewactions

import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.listener
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.fnives.test.showcase.testutils.doBlockinglyOnMainThread
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.Description
import org.hamcrest.Matcher

// swipe-refresh-layout swipe-down doesn't work, inspired by https://github.com/robolectric/robolectric/issues/5375
class PullToRefresh : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return object : BaseMatcher<View>() {
            override fun matches(item: Any): Boolean {
                return isA(SwipeRefreshLayout::class.java).matches(item)
            }

            override fun describeMismatch(item: Any, mismatchDescription: Description) {
                mismatchDescription.appendText("Expected SwipeRefreshLayout or its Descendant, but got other View")
            }

            override fun describeTo(description: Description) {
                description.appendText("Action SwipeToRefresh to view SwipeRefreshLayout or its descendant")
            }
        }
    }

    override fun getDescription(): String {
        return "Perform pull-to-refresh on the SwipeRefreshLayout"
    }

    override fun perform(uiController: UiController, view: View) {
        val swipeRefreshLayout = view as SwipeRefreshLayout
        doBlockinglyOnMainThread {
            swipeRefreshLayout.isRefreshing = true
            swipeRefreshLayout.listener().onRefresh()
        }
    }
}
