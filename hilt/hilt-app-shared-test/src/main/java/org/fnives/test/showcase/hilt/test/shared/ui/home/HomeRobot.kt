package org.fnives.test.showcase.hilt.test.shared.ui.home

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.fnives.test.showcase.android.testutil.intent.notIntended
import org.fnives.test.showcase.android.testutil.viewaction.imageview.WithDrawable
import org.fnives.test.showcase.android.testutil.viewaction.recycler.RemoveItemAnimations
import org.fnives.test.showcase.android.testutil.viewaction.swiperefresh.PullToRefresh
import org.fnives.test.showcase.hilt.R
import org.fnives.test.showcase.hilt.ui.auth.AuthActivity
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.FavouriteContent
import org.hamcrest.Matchers.allOf

class HomeRobot {

    /**
     * Needed because Espresso idling sometimes not in sync with RecyclerView's animation.
     * So we simply remove the item animations, the animations should be disabled anyway for test.
     *
     * Reference: https://github.com/android/android-test/issues/223
     */
    fun removeItemAnimations() = apply {
        Espresso.onView(withId(R.id.recycler)).perform(RemoveItemAnimations())
    }

    fun assertToolbarIsShown() = apply {
        Espresso.onView(withId(R.id.toolbar))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    fun setupIntentResults() {
        Intents.intending(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
    }

    fun assertNavigatedToAuth() = apply {
        Intents.intended(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
    }

    fun assertDidNotNavigateToAuth() = apply {
        notIntended(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
    }

    fun clickSignOut(setupIntentResults: Boolean = true) = apply {
        if (setupIntentResults) {
            Intents.intending(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
        }

        Espresso.onView(withId(R.id.logout_cta)).perform(click())
    }

    fun assertContainsItem(index: Int, item: FavouriteContent) = apply {
        removeItemAnimations()
        val isFavouriteResourceId = if (item.isFavourite) {
            R.drawable.favorite_24
        } else {
            R.drawable.favorite_border_24
        }
        Espresso.onView(withId(R.id.recycler))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))

        Espresso.onView(
            allOf(
                withChild(allOf(withText(item.content.title), withId(R.id.title))),
                withChild(allOf(withText(item.content.description), withId(R.id.description))),
                withChild(allOf(withId(R.id.favourite_cta), WithDrawable(isFavouriteResourceId)))
            )
        )
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    fun clickOnContentItem(index: Int, item: Content) = apply {
        removeItemAnimations()
        Espresso.onView(withId(R.id.recycler))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))

        Espresso.onView(
            allOf(
                withId(R.id.favourite_cta),
                withParent(
                    allOf(
                        withChild(allOf(withText(item.title), withId(R.id.title))),
                        withChild(allOf(withText(item.description), withId(R.id.description)))
                    )
                )
            )
        )
            .perform(click())
    }

    fun swipeRefresh() = apply {
        Espresso.onView(withId(R.id.swipe_refresh_layout)).perform(PullToRefresh())
    }

    fun assertContainsNoItems() = apply {
        removeItemAnimations()
        Espresso.onView(withId(R.id.recycler))
            .check(matches(hasChildCount(0)))
    }

    fun assertContainsError() = apply {
        Espresso.onView(withId(R.id.error_message))
            .check(matches(allOf(isDisplayed(), withText(R.string.something_went_wrong))))
    }
}
