package org.fnives.test.showcase.hilt.ui.compose

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.fnives.test.showcase.android.testutil.intent.DismissSystemDialogsRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.hilt.R
import org.fnives.test.showcase.hilt.compose.screen.AppNavigation
import org.fnives.test.showcase.hilt.core.integration.fake.FakeUserDataLocalStorage
import org.fnives.test.showcase.hilt.di.TestUserDataLocalStorageModule
import org.fnives.test.showcase.hilt.test.shared.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.hilt.test.shared.testutils.idling.DatabaseDispatcherTestRule
import org.fnives.test.showcase.hilt.ui.compose.idle.ComposeNetworkSyncHelper
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthComposeInstrumentedTest {

    private val composeTestRule = createComposeRule()
    private val stateRestorationTester = StateRestorationTester(composeTestRule)

    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
    private val dispatcherTestRule = DatabaseDispatcherTestRule()
    private lateinit var robot: ComposeLoginRobot
    private lateinit var navigationRobot: ComposeNavigationRobot

    @Inject
    lateinit var composeNetworkSyncHelper: ComposeNetworkSyncHelper

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(DismissSystemDialogsRule())
        .around(mockServerScenarioSetupTestRule)
        .around(dispatcherTestRule)
        .around(composeTestRule)
        .around(ScreenshotRule("test-showcase-compose"))

    @Before
    fun setup() {
        TestUserDataLocalStorageModule.replacement = FakeUserDataLocalStorage()
        hiltRule.inject()

        stateRestorationTester.setContent {
            AppNavigation()
        }
        robot = ComposeLoginRobot(composeTestRule)
        navigationRobot = ComposeNavigationRobot(composeTestRule)
        composeNetworkSyncHelper.setup(composeTestRule)
    }

    @After
    fun tearDown() {
        composeNetworkSyncHelper.tearDown()
    }

    /** GIVEN non empty password and username and successful response WHEN signIn THEN no error is shown and navigating to home */
    @Test
    fun properLoginResultsInNavigationToHome() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.Success(password = "alma", username = "banan")
        )
        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY)

        navigationRobot.assertAuthScreen()
        robot.setPassword("alma")
            .setUsername("banan")
            .assertUsername("banan")
            .assertPassword("alma")

        composeTestRule.mainClock.autoAdvance = false
        robot.clickOnLogin()
        composeTestRule.mainClock.advanceTimeByFrame()
        robot.assertLoading()
        composeTestRule.mainClock.autoAdvance = true

        navigationRobot.assertHomeScreen()
    }

    @Test
    fun emptyPasswordShowsProperErrorMessage() {
        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY)
        navigationRobot.assertAuthScreen()

        robot.setUsername("banan")
            .assertUsername("banan")
            .clickOnLogin()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = true

        robot.assertErrorIsShown(R.string.password_is_invalid)
            .assertNotLoading()
        navigationRobot.assertAuthScreen()
    }

    /** GIVEN password and empty username WHEN signIn THEN error username is shown */
    @Test
    fun emptyUserNameShowsProperErrorMessage() {
        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY)
        navigationRobot.assertAuthScreen()

        robot
            .setPassword("banan")
            .assertPassword("banan")
            .clickOnLogin()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = true

        robot.assertErrorIsShown(R.string.username_is_invalid)
            .assertNotLoading()
        navigationRobot.assertAuthScreen()
    }

    /** GIVEN password and username and invalid credentials response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun invalidCredentialsGivenShowsProperErrorMessage() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.InvalidCredentials(password = "alma", username = "banan")
        )

        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY)
        navigationRobot.assertAuthScreen()
        robot.setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")

        composeTestRule.mainClock.autoAdvance = false
        robot.clickOnLogin()
        composeTestRule.mainClock.advanceTimeByFrame()
        robot.assertLoading()
        composeTestRule.mainClock.autoAdvance = true

        robot.assertErrorIsShown(R.string.credentials_invalid)
            .assertNotLoading()
        navigationRobot.assertAuthScreen()
    }

    /** GIVEN password and username and error response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun networkErrorShowsProperErrorMessage() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.GenericError(username = "alma", password = "banan")
        )

        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY)
        navigationRobot.assertAuthScreen()
        robot.setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")

        composeTestRule.mainClock.autoAdvance = false
        robot.clickOnLogin()
        composeTestRule.mainClock.advanceTimeByFrame()
        robot.assertLoading()
        composeTestRule.mainClock.autoAdvance = true

        robot.assertErrorIsShown(R.string.something_went_wrong)
            .assertNotLoading()
        navigationRobot.assertAuthScreen()
    }

    /** GIVEN username and password WHEN restoring THEN username and password fields contain the same text */
    @Test
    fun restoringContentShowPreviousCredentials() {
        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY)
        navigationRobot.assertAuthScreen()
        robot.setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")

        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.mainClock.advanceTimeBy(SPLASH_DELAY) // ensure all time based operation run

        navigationRobot.assertAuthScreen()
        robot.assertUsername("alma")
            .assertPassword("banan")
    }

    companion object {
        private const val SPLASH_DELAY = 600L
    }
}
