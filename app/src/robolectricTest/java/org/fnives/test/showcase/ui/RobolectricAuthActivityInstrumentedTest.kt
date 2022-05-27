package org.fnives.test.showcase.ui

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.R
import org.fnives.test.showcase.android.testutil.activity.safeClose
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.CompositeDisposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.Disposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.IdlingResourceDisposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.OkHttp3IdlingResource
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.testutil.NetworkTestConfigurationHelper
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule.Companion.advanceUntilIdleWithIdlingResources
import org.fnives.test.showcase.testutils.storage.TestDatabaseInitialization
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RobolectricAuthActivityInstrumentedTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<AuthActivity>
    private lateinit var robot: RobolectricLoginRobot
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockServerScenarioSetup: MockServerScenarioSetup
    private lateinit var disposable: Disposable

    @Before
    fun setup() {
        Intents.init()
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        testDispatcher = dispatcher
        TestDatabaseInitialization.overwriteDatabaseInitialization(dispatcher)

        mockServerScenarioSetup = NetworkTestConfigurationHelper.startWithHTTPSMockWebServer()

        val idlingResources = NetworkTestConfigurationHelper.getOkHttpClients()
            .associateBy(keySelector = { it.toString() })
            .map { (key, client) -> OkHttp3IdlingResource.create(key, client) }
            .map(::IdlingResourceDisposable)
        disposable = CompositeDisposable(idlingResources)

        robot = RobolectricLoginRobot()
        activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        mockServerScenarioSetup.stop()
        disposable.dispose()
        activityScenario.safeClose()
        Intents.release()
    }

    /** GIVEN non empty password and username and successful response WHEN signIn THEN no error is shown and navigating to home */
    @Test
    fun properLoginResultsInNavigationToHome() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.Success(password = "alma", username = "banan"),
            validateArguments = true
        )

        robot.setPassword("alma")
            .setUsername("banan")
            .assertPassword("alma")
            .assertUsername("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()
            .assertErrorIsNotShown()

        testDispatcher.advanceUntilIdleWithIdlingResources()
        robot.assertNavigatedToHome()
    }

    /** GIVEN empty password and username WHEN signIn THEN error password is shown */
    @Test
    fun emptyPasswordShowsProperErrorMessage() {
        robot.setUsername("banan")
            .assertUsername("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()
            .assertErrorIsNotShown()

        testDispatcher.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.password_is_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    /** GIVEN password and empty username WHEN signIn THEN error username is shown */
    @Test
    fun emptyUserNameShowsProperErrorMessage() {
        robot.setPassword("banan")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()

        testDispatcher.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.username_is_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    /** GIVEN password and username and invalid credentials response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun invalidCredentialsGivenShowsProperErrorMessage() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.InvalidCredentials(username = "alma", password = "banan"),
            validateArguments = true
        )
        robot
            .setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()
            .assertErrorIsNotShown()

        testDispatcher.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.credentials_invalid)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }

    /** GIVEN password and username and error response WHEN signIn THEN error invalid credentials is shown */
    @Test
    fun networkErrorShowsProperErrorMessage() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.GenericError(username = "alma", password = "banan"),
            validateArguments = true
        )
        robot
            .setUsername("alma")
            .setPassword("banan")
            .assertUsername("alma")
            .assertPassword("banan")
            .clickOnLogin()
            .assertLoadingBeforeRequests()
            .assertErrorIsNotShown()

        testDispatcher.advanceUntilIdleWithIdlingResources()
        robot.assertErrorIsShown(R.string.something_went_wrong)
            .assertNotNavigatedToHome()
            .assertNotLoading()
    }
}
