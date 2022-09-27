package org.fnives.test.showcase.hilt.test.shared.ui.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import org.fnives.test.showcase.android.testutil.activity.SafeCloseActivityRule
import org.fnives.test.showcase.android.testutil.intent.DismissSystemDialogsRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.hilt.R
import org.fnives.test.showcase.hilt.test.shared.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.hilt.test.shared.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.hilt.test.shared.ui.NetworkSynchronizedActivityTest
import org.fnives.test.showcase.hilt.ui.auth.AuthActivity
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@Suppress("TestFunctionName")
open class AuthActivityInstrumentedSharedTest : NetworkSynchronizedActivityTest() {

    private lateinit var activityScenario: ActivityScenario<AuthActivity>

    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()
    private val mockServerScenarioSetup: MockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
    private lateinit var robot: LoginRobot

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(DismissSystemDialogsRule())
        .around(mockServerScenarioSetupTestRule)
        .around(mainDispatcherTestRule)
        .around(SafeCloseActivityRule { activityScenario })
        .around(ScreenshotRule("test-showcase"))

    override fun setupAfterInjection() {
        Intents.init()
        robot = LoginRobot()
    }

    override fun additionalTearDown() {
        Intents.release()
    }

    /** GIVEN non empty password and username and successful response WHEN signIn THEN no error is shown and navigating to home */
    @Test
    fun properLoginResultsInNavigationToHome() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.Success(password = "alma", username = "banan")
        )
        activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        robot
            .setPassword("alma")
            .setUsername("banan")
            .assertPassword("alma")
            .assertUsername("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.assertNavigatedToHome()
    }

    /** GIVEN empty password and username WHEN signIn THEN error password is shown */
    @Test
    fun emptyPasswordShowsProperErrorMessage() {
        activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        robot
            .setUsername("banan")
            .assertUsername("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.password_is_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    /** GIVEN password and empty username WHEN signIn THEN error username is shown */
    @Test
    fun emptyUserNameShowsProperErrorMessage() {
        activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        robot
            .setPassword("banan")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.username_is_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    /** GIVEN password and username and invalid credentials response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun invalidCredentialsGivenShowsProperErrorMessage() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.InvalidCredentials(username = "alma", password = "banan")
        )
        activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        robot
            .setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.credentials_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    /** GIVEN password and username and error response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun networkErrorShowsProperErrorMessage() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.GenericError(username = "alma", password = "banan")
        )
        activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        robot
            .setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.something_went_wrong)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }
}
