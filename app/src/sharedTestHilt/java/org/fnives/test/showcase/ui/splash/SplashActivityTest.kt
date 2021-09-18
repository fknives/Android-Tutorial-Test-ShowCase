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
import org.fnives.test.showcase.testutils.statesetup.SetupLoggedInState
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
    lateinit var setupLoggedInState: SetupLoggedInState

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

    @Test
    fun GIVEN_loggedInState_WHEN_opened_THEN_MainActivity_is_started() {
        setupLoggedInState.setupLogin(mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        activityScenario = ActivityScenario.launch(HiltSplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(500)

        splashRobot.assertHomeIsStarted()
            .assertAuthIsNotStarted()

        setupLoggedInState.setupLogout()
    }

    @Test
    fun GIVEN_loggedOffState_WHEN_opened_THEN_AuthActivity_is_started() {
        setupLoggedInState.setupLogout()

        activityScenario = ActivityScenario.launch(HiltSplashActivity::class.java)

        mainDispatcherTestRule.advanceTimeBy(500)

        splashRobot.assertAuthIsStarted()
            .assertHomeIsNotStarted()
    }
}
