package org.fnives.test.showcase.ui.login

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.fnives.test.showcase.R
import org.fnives.test.showcase.testutils.configuration.SnackbarVerificationHelper
import org.fnives.test.showcase.testutils.viewactions.ReplaceProgressBarDrawableToStatic
import org.fnives.test.showcase.testutils.viewactions.notIntended
import org.fnives.test.showcase.ui.home.MainActivity
import org.hamcrest.core.IsNot.not

class LoginRobot(
    private val snackbarVerificationHelper: SnackbarVerificationHelper = SnackbarVerificationHelper()
) {

    fun setupIntentResults() {
        Intents.intending(hasComponent(MainActivity::class.java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
    }

    /**
     * Needed because Espresso idling waits until mainThread is idle.
     *
     * However, ProgressBar keeps the main thread active since it's animating.
     *
     * Another solution is described here: https://proandroiddev.com/progressbar-animations-with-espresso-57f826102187
     * In short they replace the inflater to remove animations, by using custom test runner.
     */
    fun replaceProgressBar() = apply {
        onView(withId(R.id.loading_indicator)).perform(ReplaceProgressBarDrawableToStatic())
    }

    fun setUsername(username: String): LoginRobot = apply {
        onView(withId(R.id.user_edit_text))
            .perform(ViewActions.replaceText(username), ViewActions.closeSoftKeyboard())
    }

    fun setPassword(password: String): LoginRobot = apply {
        onView(withId(R.id.password_edit_text))
            .perform(ViewActions.replaceText(password), ViewActions.closeSoftKeyboard())
    }

    fun clickOnLogin() = apply {
        replaceProgressBar()
        onView(withId(R.id.login_cta))
            .perform(ViewActions.click())
    }

    fun assertPassword(password: String) = apply {
        onView(withId((R.id.password_edit_text)))
            .check(ViewAssertions.matches(ViewMatchers.withText(password)))
    }

    fun assertUsername(username: String) = apply {
        onView(withId((R.id.user_edit_text)))
            .check(ViewAssertions.matches(ViewMatchers.withText(username)))
    }

    fun assertErrorIsShown(@StringRes stringResID: Int) = apply {
        snackbarVerificationHelper.assertIsShownWithText(stringResID)
    }

    fun assertLoadingBeforeRequests() = apply {
        onView(withId(R.id.loading_indicator))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    fun assertNotLoading() = apply {
        onView(withId(R.id.loading_indicator))
            .check(ViewAssertions.matches(not(isDisplayed())))
    }

    fun assertErrorIsNotShown() = apply {
        snackbarVerificationHelper.assertIsNotShown()
    }

    fun assertNavigatedToHome() = apply {
        intended(hasComponent(MainActivity::class.java.canonicalName))
    }

    fun assertNotNavigatedToHome() = apply {
        notIntended(hasComponent(MainActivity::class.java.canonicalName))
    }
}
