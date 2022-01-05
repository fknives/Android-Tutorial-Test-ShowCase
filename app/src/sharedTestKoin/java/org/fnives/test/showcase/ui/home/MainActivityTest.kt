package org.fnives.test.showcase.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.testutils.ReloadKoinModulesIfNecessaryTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.fnives.test.showcase.testutils.idling.loopMainThreadFor
import org.fnives.test.showcase.testutils.idling.loopMainThreadUntilIdleWithIdlingResources
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.fnives.test.showcase.testutils.statesetup.SetupLoggedInState
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class MainActivityTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val snackbarVerificationTestRule =
        SpecificTestConfigurationsFactory.createSnackbarVerification()

    @Rule
    @JvmField
    val robotRule = RobotTestRule(HomeRobot())
    private val homeRobot get() = robotRule.robot

    @Rule
    @JvmField
    val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()

    @Rule
    @JvmField
    val mainDispatcherTestRule = SpecificTestConfigurationsFactory.createMainDispatcherTestRule()

    @Rule
    @JvmField
    val reloadKoinModulesIfNecessaryTestRule = ReloadKoinModulesIfNecessaryTestRule()

    private lateinit var disposable: Disposable

    @Before
    fun setUp() {
        SpecificTestConfigurationsFactory.createServerTypeConfiguration()
            .invoke(mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        SetupLoggedInState.setupLogin(mockServerScenarioSetupTestRule.mockServerScenarioSetup)
        disposable = NetworkSynchronization.registerNetworkingSynchronization()
    }

    @After
    fun tearDown() {
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        disposable.dispose()
    }

    /** GIVEN initialized MainActivity WHEN signout is clicked THEN user is signed out */
    @Test
    fun signOutClickedResultsInNavigation() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Error(false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.clickSignOut()
        mainDispatcherTestRule.advanceUntilIdleOrActivityIsDestroyed()

        homeRobot.assertNavigatedToAuth()
        Assert.assertEquals(false, SetupLoggedInState.isLoggedIn())
    }

    /** GIVEN success response WHEN data is returned THEN it is shown on the ui */
    @Test
    fun successfulDataLoadingShowsTheElementsOnTheUI() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        ContentData.contentSuccess.forEachIndexed { index, content ->
            homeRobot.assertContainsItem(index, FavouriteContent(content, false))
        }
        homeRobot.assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked THEN ui is updated */
    @Test
    fun clickingOnListElementUpdatesTheElementsFavouriteState() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)
        homeRobot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked THEN ui is updated even if activity is recreated */
    @Test
    fun elementFavouritedIsKeptEvenIfActivityIsRecreated() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)

        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked then clicked again THEN ui is updated */
    @Test
    fun clickingAnElementMultipleTimesProperlyUpdatesIt() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), false)
        homeRobot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN error response WHEN loaded THEN error is Shown */
    @Test
    fun networkErrorResultsInUIErrorStateShown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Error(false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertContainsNoItems()
            .assertContainsError()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN error response then success WHEN retried THEN success is shown */
    @Test
    fun retryingFromErrorStateAndSucceedingShowsTheData() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(
                ContentScenario.Error(false)
                    .then(ContentScenario.Success(false))
            )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.swipeRefresh()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loopMainThreadFor(2000L)

        ContentData.contentSuccess.forEachIndexed { index, content ->
            homeRobot.assertContainsItem(index, FavouriteContent(content, false))
        }
        homeRobot.assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(
                ContentScenario.Success(false)
                    .then(ContentScenario.Error(false))
            )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.swipeRefresh()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loopMainThreadUntilIdleWithIdlingResources()
        mainDispatcherTestRule.advanceTimeBy(1000L)
        loopMainThreadFor(1000)

        homeRobot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN unauthenticated then success WHEN loaded THEN success is shown */
    @Test
    fun authenticationIsHandledWithASingleLoading() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(
                ContentScenario.Unauthorized(false)
                    .then(ContentScenario.Success(true))
            )
            .setScenario(RefreshTokenScenario.Success)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        ContentData.contentSuccess.forEachIndexed { index, content ->
            homeRobot.assertContainsItem(index, FavouriteContent(content, false))
        }
        homeRobot.assertDidNotNavigateToAuth()
    }

    /** GIVEN unauthenticated then error WHEN loaded THEN navigated to auth */
    @Test
    fun sessionExpirationResultsInNavigation() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Unauthorized(false))
            .setScenario(RefreshTokenScenario.Error)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertNavigatedToAuth()
        Assert.assertEquals(false, SetupLoggedInState.isLoggedIn())
    }
}
