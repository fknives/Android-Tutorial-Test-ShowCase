package org.fnives.test.showcase.ui.home

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupTestRule
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.fnives.test.showcase.testutils.idling.loopMainThreadFor
import org.fnives.test.showcase.testutils.idling.loopMainThreadUntilIdleWithIdlingResources
import org.fnives.test.showcase.testutils.robot.RobotTestRule
import org.junit.After
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
    val robotRule = RobotTestRule(HomeRobot())
    private val homeRobot get() = robotRule.robot

    @Rule
    @JvmField
    val mockServerScenarioSetupTestRule = MockServerScenarioSetupTestRule()

    val mockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup

    @Rule
    @JvmField
    val mainDispatcherTestRule = SpecificTestConfigurationsFactory.createMainDispatcherTestRule()

    private lateinit var disposable: Disposable

    @Before
    fun setUp() {

        disposable = NetworkSynchronization.registerNetworkingSynchronization()
        homeRobot.setupLogin(
            mainDispatcherTestRule,
            mockServerScenarioSetup
        )
    }

    @After
    fun tearDown() {
        activityScenario.close()
        disposable.dispose()
    }

    /** GIVEN initialized MainActivity WHEN signout is clicked THEN user is signed out */
    @Test
    fun signOutClickedResultsInNavigation() {
        mockServerScenarioSetup.setScenario(ContentScenario.Error(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.clickSignOut()
        mainDispatcherTestRule.advanceUntilIdleOrActivityIsDestroyed()

        homeRobot.assertNavigatedToAuth()
    }

    /** GIVEN success response WHEN data is returned THEN it is shown on the ui */
    @Test
    fun successfulDataLoadingShowsTheElementsOnTheUI() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
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
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
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
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)

        activityScenario.close()
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked then clicked again THEN ui is updated */
    @Test
    fun clickingAnElementMultipleTimesProperlyUpdatesIt() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
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
        mockServerScenarioSetup.setScenario(ContentScenario.Error(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertContainsNoItems()
            .assertContainsError()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN error response then success WHEN retried THEN success is shown */
    @Test
    fun retryingFromErrorStateAndSucceedingShowsTheData() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Error(usingRefreshedToken = false)
                .then(ContentScenario.Success(usingRefreshedToken = false))
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
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
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
        mockServerScenarioSetup.setScenario(
            ContentScenario.Unauthorized(usingRefreshedToken = false)
                .then(ContentScenario.Success(usingRefreshedToken = true))
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
        mockServerScenarioSetup.setScenario(ContentScenario.Unauthorized(usingRefreshedToken = false))
            .setScenario(RefreshTokenScenario.Error)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertNavigatedToAuth()
    }
}
