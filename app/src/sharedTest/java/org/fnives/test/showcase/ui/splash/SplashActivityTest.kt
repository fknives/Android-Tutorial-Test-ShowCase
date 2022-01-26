package org.fnives.test.showcase.ui.splash

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.configuration.MainDispatcherTestRule
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.fnives.test.showcase.testutils.statesetup.SetupAuthenticationState.setupLogin
import org.fnives.test.showcase.testutils.statesetup.SetupAuthenticationState.setupLogout
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class SplashActivityTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<SplashActivity>

    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()

    private val robot = SplashRobot()

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(mockServerScenarioSetupTestRule)
        .around(mainDispatcherTestRule)
        .around(RobotTestRule(robot))

    @After
    fun tearDown() {
        activityScenario.close()
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

        workaroundForActivityScenarioCLoseLockingUp()
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

        workaroundForActivityScenarioCLoseLockingUp()
    }

    /** GIVEN loggedOffState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedOutStatesNotEnoughTime() {
        setupLogout(mainDispatcherTestRule)
        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(10)

        robot.assertAuthIsNotStarted()
            .assertHomeIsNotStarted()
    }

    /** GIVEN loggedInState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedInStatesNotEnoughTime() {
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(10)

        robot.assertHomeIsNotStarted()
            .assertAuthIsNotStarted()
    }

    /**
     * This should not be needed, we shouldn't use sleep ever.
     * However, it seems to be and issue described here: https://github.com/android/android-test/issues/676
     *
     * If an activity is finished in code, the ActivityScenario.close() can hang 30 to 45 seconds.
     * This sleeps let's the Activity finish it state change and unlocks the ActivityScenario.
     *
     * As soon as that issue is closed, this should be removed as well.
     */
    private fun workaroundForActivityScenarioCLoseLockingUp() {
        Thread.sleep(1000L)
    }
}
