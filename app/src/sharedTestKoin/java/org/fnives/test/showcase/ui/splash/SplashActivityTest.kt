package org.fnives.test.showcase.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.testutils.ReloadKoinModulesIfNecessaryTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.fnives.test.showcase.testutils.statesetup.SetupLoggedInState
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class SplashActivityTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<SplashActivity>

    private val splashRobot: SplashRobot get() = robotTestRule.robot

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val robotTestRule = RobotTestRule(SplashRobot())

    @Rule
    @JvmField
    val mainDispatcherTestRule = SpecificTestConfigurationsFactory.createMainDispatcherTestRule()

    @Rule
    @JvmField
    val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()

    @Rule
    @JvmField
    val reloadKoinModulesIfNecessaryTestRule = ReloadKoinModulesIfNecessaryTestRule()

    lateinit var disposable: Disposable

    @Before
    fun setUp() {
        SpecificTestConfigurationsFactory.createServerTypeConfiguration()
            .invoke(mockServerScenarioSetupTestRule.mockServerScenarioSetup)
        disposable = NetworkSynchronization.registerNetworkingSynchronization()
    }

    @After
    fun tearDown() {
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        disposable.dispose()
    }

    /** GIVEN loggedInState WHEN opened THEN MainActivity is started */
    @Test
    fun loggedInStateNavigatesToHome() {
        SetupLoggedInState.setupLogin(mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(500)

        splashRobot.assertHomeIsStarted()
            .assertAuthIsNotStarted()

        SetupLoggedInState.setupLogout()
    }

    /** GIVEN loggedOffState WHEN opened THEN AuthActivity is started */
    @Test
    fun loggedOutStatesNavigatesToAuthentication() {
        SetupLoggedInState.setupLogout()

        activityScenario = ActivityScenario.launch(SplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(500)

        splashRobot.assertAuthIsStarted()
            .assertHomeIsNotStarted()
    }
}
