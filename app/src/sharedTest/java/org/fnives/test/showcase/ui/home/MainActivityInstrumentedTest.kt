package org.fnives.test.showcase.ui.home

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.testutils.idling.loopMainThreadFor
import org.fnives.test.showcase.testutils.idling.loopMainThreadUntilIdleWithIdlingResources
import org.fnives.test.showcase.testutils.safeClose
import org.fnives.test.showcase.testutils.statesetup.SetupAuthenticationState.setupLogin
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()
    private val mockServerScenarioSetup
        get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private lateinit var robot : HomeRobot

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(mockServerScenarioSetupTestRule)
        .around(mainDispatcherTestRule)

    @Before
    fun setup() {
        robot = HomeRobot()
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetup)
        Intents.init()
    }

    @After
    fun tearDown() {
        activityScenario.safeClose()
        Intents.release()
    }

    /** GIVEN initialized MainActivity WHEN signout is clicked THEN user is signed out */
    @Test
    fun signOutClickedResultsInNavigation() {
        mockServerScenarioSetup.setScenario(ContentScenario.Error(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        robot.clickSignOut()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        robot.assertNavigatedToAuth()
    }

    /** GIVEN success response WHEN data is returned THEN it is shown on the ui */
    @Test
    fun successfulDataLoadingShowsTheElementsOnTheUI() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        ContentData.contentSuccess.forEachIndexed { index, content ->
            robot.assertContainsItem(index, FavouriteContent(content, false))
        }
        robot.assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked THEN ui is updated */
    @Test
    fun clickingOnListElementUpdatesTheElementsFavouriteState() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)
        robot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked THEN ui is updated even if activity is recreated */
    @Test
    fun elementFavouritedIsKeptEvenIfActivityIsRecreated() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)

        activityScenario.safeClose()
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        robot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked then clicked again THEN ui is updated */
    @Test
    fun clickingAnElementMultipleTimesProperlyUpdatesIt() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), false)
        robot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN error response WHEN loaded THEN error is Shown */
    @Test
    fun networkErrorResultsInUIErrorStateShown() {
        mockServerScenarioSetup.setScenario(ContentScenario.Error(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        robot.assertContainsNoItems()
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

        robot.swipeRefresh()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loopMainThreadFor(2000L)

        ContentData.contentSuccess.forEachIndexed { index, content ->
            robot.assertContainsItem(index, FavouriteContent(content, false))
        }
        robot.assertDidNotNavigateToAuth()
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

        robot.swipeRefresh()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loopMainThreadUntilIdleWithIdlingResources()
        mainDispatcherTestRule.advanceTimeBy(1000L)
        loopMainThreadFor(1000)

        robot
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
            robot.assertContainsItem(index, FavouriteContent(content, false))
        }
        robot.assertDidNotNavigateToAuth()
    }

    /** GIVEN unauthenticated then error WHEN loaded THEN navigated to auth */
    @Test
    fun sessionExpirationResultsInNavigation() {
        mockServerScenarioSetup.setScenario(ContentScenario.Unauthorized(usingRefreshedToken = false))
            .setScenario(RefreshTokenScenario.Error)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        robot.assertNavigatedToAuth()
    }
}
