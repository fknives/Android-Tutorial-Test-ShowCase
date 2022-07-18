package org.fnives.test.showcase.ui.login

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.R
import org.fnives.test.showcase.android.testutil.activity.SafeCloseActivityRule
import org.fnives.test.showcase.android.testutil.intent.DismissSystemDialogsRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
@Ignore("verifying compose")
class AuthActivityInstrumentedTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<AuthActivity>

    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private lateinit var robot: LoginRobot

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(DismissSystemDialogsRule())
        .around(mockServerScenarioSetupTestRule)
        .around(mainDispatcherTestRule)
        .around(SafeCloseActivityRule { activityScenario })
        .around(ScreenshotRule("test-showcase"))

    @Before
    fun setup() {
        Intents.init()
        robot = LoginRobot()
    }

    @After
    fun tearDown() {
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
