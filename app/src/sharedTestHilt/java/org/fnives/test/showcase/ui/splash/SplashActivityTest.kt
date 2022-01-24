package org.fnives.test.showcase.ui.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.fnives.test.showcase.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import javax.inject.Inject

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SplashActivityTest : KoinTest {

    private var activityScenario: ActivityScenario<HiltSplashActivity>? = null

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
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var networkSynchronization: NetworkSynchronization

    var disposable: Disposable? = null

    @Before
    fun setUp() {
        SpecificTestConfigurationsFactory.createServerTypeConfiguration()
            .invoke(mockServerScenarioSetupTestRule.mockServerScenarioSetup)
        hiltRule.inject()
        disposable = networkSynchronization.registerNetworkingSynchronization()
    }

    @After
    fun tearDown() {
        activityScenario?.moveToState(Lifecycle.State.DESTROYED)
        disposable?.dispose()
    }

    /** GIVEN loggedInState WHEN opened THEN MainActivity is started */
    @Test
    fun loggedInStateNavigatesToHome() {
        splashRobot.setupLoggedInState(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(HiltSplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(501)

        splashRobot.assertHomeIsStarted()
            .assertAuthIsNotStarted()
    }

    /** GIVEN loggedOffState WHEN opened THEN AuthActivity is started */
    @Test
    fun loggedOutStatesNavigatesToAuthentication() {
        splashRobot.setupLoggedOutState(mainDispatcherTestRule)

        activityScenario = ActivityScenario.launch(HiltSplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(501)

        splashRobot.assertAuthIsStarted()
            .assertHomeIsNotStarted()
    }

    @Test
    fun loggedOutStatesNotEnoughTime() {
        splashRobot.setupLoggedOutState(mainDispatcherTestRule)

        activityScenario = ActivityScenario.launch(HiltSplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(10)

        splashRobot.assertAuthIsNotStarted()
            .assertHomeIsNotStarted()
    }

    /** GIVEN loggedInState and not enough time WHEN opened THEN no activity is started */
    @Test
    fun loggedInStatesNotEnoughTime() {
        splashRobot.setupLoggedInState(mainDispatcherTestRule, mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(HiltSplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(10)

        splashRobot.assertHomeIsNotStarted()
            .assertAuthIsNotStarted()
    }
}
