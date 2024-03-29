package org.fnives.test.showcase.ui.splash

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import org.fnives.test.showcase.android.testutil.activity.SafeCloseActivityRule
import org.fnives.test.showcase.android.testutil.intent.DismissSystemDialogsRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.testutils.statesetup.SetupAuthenticationState.setupLogin
import org.fnives.test.showcase.testutils.statesetup.SetupAuthenticationState.setupLogout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
abstract class SplashActivityInstrumentedSharedTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<SplashActivity>

    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()

    private lateinit var robot: SplashRobot

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
        robot = SplashRobot()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /** GIVEN loggedInState WHEN opened after some time THEN MainActivity is started */
    @Test
    fun loggedInStateNavigatesToHome() {
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(501)

        robot.assertHomeIsStarted()
            .assertAuthIsNotStarted()
    }

    /** GIVEN loggedOffState WHEN opened after some time THEN AuthActivity is started */
    @Test
    fun loggedOutStatesNavigatesToAuthentication() {
        setupLogout(mainDispatcherTestRule)
        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(501)

        robot.assertAuthIsStarted()
            .assertHomeIsNotStarted()
    }

    /** GIVEN loggedOffState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedOutStatesNotEnoughTime() {
        setupLogout(mainDispatcherTestRule)
        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(500)

        robot.assertAuthIsNotStarted()
            .assertHomeIsNotStarted()
    }

    /** GIVEN loggedInState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedInStatesNotEnoughTime() {
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(500)

        robot.assertHomeIsNotStarted()
            .assertAuthIsNotStarted()
    }
}
