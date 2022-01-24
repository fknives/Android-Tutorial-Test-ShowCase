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
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.fnives.test.showcase.R
import org.fnives.test.showcase.testutils.configuration.LoginRobotConfiguration
import org.fnives.test.showcase.testutils.configuration.SnackbarVerificationTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.configuration.TestConfigurationsFactory
import org.fnives.test.showcase.testutils.robot.Robot
import org.fnives.test.showcase.testutils.viewactions.notIntended
import org.fnives.test.showcase.ui.ActivityClassHolder
import org.hamcrest.core.IsNot.not

class LoginRobot(
    private val loginRobotConfiguration: LoginRobotConfiguration,
    private val snackbarVerificationTestRule: SnackbarVerificationTestRule
) : Robot {

    constructor(testConfigurationsFactory: TestConfigurationsFactory = SpecificTestConfigurationsFactory) :
        this(
            loginRobotConfiguration = testConfigurationsFactory.createLoginRobotConfiguration(),
            snackbarVerificationTestRule = testConfigurationsFactory.createSnackbarVerification()
        )

    override fun init() {
        Intents.init()
        setupIntentResults()
    }

    fun setupIntentResults() {
        intending(hasComponent(ActivityClassHolder.mainActivity().java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
    }

    override fun release() {
        Intents.release()
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
        snackbarVerificationTestRule.assertIsShownWithText(stringResID)
    }

    fun assertLoadingBeforeRequests() = apply {
        if (loginRobotConfiguration.assertLoadingBeforeRequest) {
            onView(withId(R.id.loading_indicator))
                .check(ViewAssertions.matches(isDisplayed()))
        }
    }

    fun assertNotLoading() = apply {
        onView(withId(R.id.loading_indicator))
            .check(ViewAssertions.matches(not(isDisplayed())))
    }

    fun assertErrorIsNotShown() = apply {
        snackbarVerificationTestRule.assertIsNotShown()
    }

    fun assertNavigatedToHome() = apply {
        intended(hasComponent(ActivityClassHolder.mainActivity().java.canonicalName))
    }

    fun assertNotNavigatedToHome() = apply {
        notIntended(hasComponent(ActivityClassHolder.mainActivity().java.canonicalName))
    }
}
