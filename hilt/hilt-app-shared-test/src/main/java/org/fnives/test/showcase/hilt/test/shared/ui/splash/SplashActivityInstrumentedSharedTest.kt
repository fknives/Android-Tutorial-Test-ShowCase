package org.fnives.test.showcase.hilt.test.shared.ui.splash

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import org.fnives.test.showcase.android.testutil.activity.SafeCloseActivityRule
import org.fnives.test.showcase.android.testutil.intent.DismissSystemDialogsRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.hilt.test.shared.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.hilt.test.shared.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.hilt.test.shared.testutils.statesetup.SetupAuthenticationState.setupLogin
import org.fnives.test.showcase.hilt.test.shared.testutils.statesetup.SetupAuthenticationState.setupLogout
import org.fnives.test.showcase.hilt.test.shared.ui.NetworkSynchronizedActivityTest
import org.fnives.test.showcase.hilt.ui.splash.SplashActivity
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@Suppress("TestFunctionName")
open class SplashActivityInstrumentedSharedTest : NetworkSynchronizedActivityTest() {

    private lateinit var activityScenario: ActivityScenario<SplashActivity>

    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()
    private val mockServerScenarioSetup: MockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup

    private lateinit var robot: SplashRobot

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(DismissSystemDialogsRule())
        .around(mainDispatcherTestRule)
        .around(mockServerScenarioSetupTestRule)
        .around(SafeCloseActivityRule { activityScenario })
        .around(ScreenshotRule("test-showcase"))

    override fun setupAfterInjection() {
        Intents.init()
        robot = SplashRobot()
    }

    override fun additionalTearDown() {
        Intents.release()
    }

    /** GIVEN loggedInState WHEN opened after some time THEN MainActivity is started */
    @Test
    fun loggedInStateNavigatesToHome() {
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetup)

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
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)

        mainDispatcherTestRule.advanceTimeBy(500)

        robot.assertHomeIsNotStarted()
            .assertAuthIsNotStarted()
    }
}
