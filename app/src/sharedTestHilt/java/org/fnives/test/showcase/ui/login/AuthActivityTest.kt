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

    /** GIVEN non empty password and username and successful response WHEN signIn THEN no error is shown and navigating to home */
    @Test
    fun properLoginResultsInNavigationToHome() {
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

    /** GIVEN empty password and username WHEN signIn THEN error password is shown */
    @Test
    fun emptyPasswordShowsProperErrorMessage() {
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

    /** GIVEN password and empty username WHEN signIn THEN error username is shown */
    @Test
    fun emptyUserNameShowsProperErrorMessage() {
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

    /** GIVEN password and username and invalid credentials response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun invalidCredentialsGivenShowsProperErrorMessage() {
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

    /** GIVEN password and username and error response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun networkErrorShowsProperErrorMessage() {
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
