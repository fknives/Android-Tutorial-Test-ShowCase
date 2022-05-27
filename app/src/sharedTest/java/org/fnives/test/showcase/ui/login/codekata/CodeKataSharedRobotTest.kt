package org.fnives.test.showcase.ui.login.codekata

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.fnives.test.showcase.R
import org.fnives.test.showcase.android.testutil.snackbar.SnackbarVerificationHelper.assertSnackBarIsNotShown
import org.fnives.test.showcase.android.testutil.snackbar.SnackbarVerificationHelper.assertSnackBarIsShownWithText
import org.fnives.test.showcase.android.testutil.intent.notIntended
import org.fnives.test.showcase.ui.home.MainActivity
import org.hamcrest.core.IsNot

class CodeKataSharedRobotTest {

    fun setUsername(username: String): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId(R.id.user_edit_text))
            .perform(ViewActions.replaceText(username), ViewActions.closeSoftKeyboard())
    }

    fun setPassword(password: String): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId(R.id.password_edit_text))
            .perform(ViewActions.replaceText(password), ViewActions.closeSoftKeyboard())
    }

    fun clickOnLogin(): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId(R.id.login_cta))
            .perform(ViewActions.click())
    }

    fun assertPassword(password: String): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId((R.id.password_edit_text)))
            .check(ViewAssertions.matches(ViewMatchers.withText(password)))
    }

    fun assertUsername(username: String): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId((R.id.user_edit_text)))
            .check(ViewAssertions.matches(ViewMatchers.withText(username)))
    }

    fun assertLoadingBeforeRequests(): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId(R.id.loading_indicator))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    fun assertNotLoading(): CodeKataSharedRobotTest = apply {
        Espresso.onView(ViewMatchers.withId(R.id.loading_indicator))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isDisplayed())))
    }

    fun assertErrorIsShown(@StringRes stringResID: Int): CodeKataSharedRobotTest = apply {
        assertSnackBarIsShownWithText(stringResID)
    }

    fun assertErrorIsNotShown(): CodeKataSharedRobotTest = apply {
        assertSnackBarIsNotShown()
    }

    fun assertNavigatedToHome(): CodeKataSharedRobotTest = apply {
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.canonicalName))
    }

    fun assertNotNavigatedToHome(): CodeKataSharedRobotTest = apply {
        notIntended(IntentMatchers.hasComponent(MainActivity::class.java.canonicalName))
    }
}
