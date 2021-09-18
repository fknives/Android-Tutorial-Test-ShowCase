package org.fnives.test.showcase.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.fnives.test.showcase.R
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.fnives.test.showcase.ui.auth.HiltAuthActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AuthActivityTest {

    private lateinit var activityScenario: ActivityScenario<HiltAuthActivity>

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val snackbarVerificationTestRule = SpecificTestConfigurationsFactory.createSnackbarVerification()

    @Rule
    @JvmField
    val robotRule = RobotTestRule(LoginRobot())
    private val loginRobot get() = robotRule.robot

    @Rule
    @JvmField
    val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()

    @Rule
    @JvmField
    val mainDispatcherTestRule = SpecificTestConfigurationsFactory.createMainDispatcherTestRule()

    @Rule
    @JvmField
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var networkSynchronization: NetworkSynchronization

    private lateinit var disposable: Disposable

    @Before
    fun setUp() {
        SpecificTestConfigurationsFactory.createServerTypeConfiguration()
            .invoke(mockServerScenarioSetupTestRule.mockServerScenarioSetup)
        hiltRule.inject()
        disposable = networkSynchronization.registerNetworkingSynchronization()
    }

    @After
    fun tearDown() {
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        disposable.dispose()
    }

    @Test
    fun GIVEN_non_empty_password_and_username_and_successful_response_WHEN_signIn_THEN_no_error_is_shown_and_navigating_to_home() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup.setScenario(
            AuthScenario.Success(
                password = "alma",
                username = "banan"
            )
        )
        activityScenario = ActivityScenario.launch(HiltAuthActivity::class.java)
        loginRobot
            .setPassword("alma")
            .setUsername("banan")
            .assertPassword("alma")
            .assertUsername("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleOrActivityIsDestroyed()
        loginRobot.assertNavigatedToHome()
    }

    @Test
    fun GIVEN_empty_password_and_username_WHEN_signIn_THEN_error_password_is_shown() {
        activityScenario = ActivityScenario.launch(HiltAuthActivity::class.java)
        loginRobot
            .setUsername("banan")
            .assertUsername("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loginRobot.assertErrorIsShown(R.string.password_is_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    @Test
    fun GIVEN_password_and_empty_username_WHEN_signIn_THEN_error_username_is_shown() {
        activityScenario = ActivityScenario.launch(HiltAuthActivity::class.java)
        loginRobot
            .setPassword("banan")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loginRobot.assertErrorIsShown(R.string.username_is_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    @Test
    fun GIVEN_password_and_username_and_invalid_credentials_response_WHEN_signIn_THEN_error_invalid_credentials_is_shown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup.setScenario(
            AuthScenario.InvalidCredentials(username = "alma", password = "banan")
        )
        activityScenario = ActivityScenario.launch(HiltAuthActivity::class.java)
        loginRobot
            .setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loginRobot.assertErrorIsShown(R.string.credentials_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    @Test
    fun GIVEN_password_and_username_and_error_response_WHEN_signIn_THEN_error_invalid_credentials_is_shown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup.setScenario(
            AuthScenario.GenericError(username = "alma", password = "banan")
        )
        activityScenario = ActivityScenario.launch(HiltAuthActivity::class.java)
        loginRobot
            .setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loginRobot.assertErrorIsShown(R.string.something_went_wrong)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }
}
