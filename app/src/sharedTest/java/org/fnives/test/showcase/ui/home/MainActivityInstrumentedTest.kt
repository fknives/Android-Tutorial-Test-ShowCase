package org.fnives.test.showcase.ui.home

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.android.testutil.activity.SafeCloseActivityRule
import org.fnives.test.showcase.android.testutil.activity.safeClose
import org.fnives.test.showcase.android.testutil.intent.DismissSystemDialogsRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.idling.AsyncDiffUtilInstantTestRule
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.testutils.statesetup.SetupAuthenticationState.setupLogin
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
    private lateinit var robot: HomeRobot

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(DismissSystemDialogsRule())
        .around(mockServerScenarioSetupTestRule)
        .around(mainDispatcherTestRule)
        .around(AsyncDiffUtilInstantTestRule())
        .around(SafeCloseActivityRule { activityScenario })
        .around(ScreenshotRule("test-showcase"))

    @Before
    fun setup() {
        robot = HomeRobot()
        setupLogin(mainDispatcherTestRule, mockServerScenarioSetup)
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    /** GIVEN initialized MainActivity WHEN signout is clicked THEN user is signed out */
    @Test
    @Ignore("a")
    fun signOutClickedResultsInNavigation() {
        mockServerScenarioSetup.setScenario(ContentScenario.Error(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        robot.clickSignOut()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        robot.assertNavigatedToAuth()
    }

    /** GIVEN success response WHEN data is returned THEN it is shown on the ui */
    @Test
    @Ignore("a")
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
    @Ignore("a")
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
    @Ignore("a")
    fun elementFavouritedIsKeptEvenIfActivityIsRecreated() {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        robot.clickOnContentItem(0, ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)

        activityScenario.safeClose()
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        robot.assertContainsItem(0, expectedItem)
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success response WHEN item is clicked then clicked again THEN ui is updated */
    @Test
    @Ignore("a")
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
    @Ignore("a")
    fun networkErrorResultsInUIErrorStateShown() {
        mockServerScenarioSetup.setScenario(ContentScenario.Error(usingRefreshedToken = false))
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        robot.assertContainsNoItems()
            .assertContainsError()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN error response then success WHEN retried THEN success is shown */
    @Test
    @Ignore("a")
    fun retryingFromErrorStateAndSucceedingShowsTheData() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Error(usingRefreshedToken = false)
                .then(ContentScenario.Success(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        ContentData.contentSuccess.forEachIndexed { index, content ->
            robot.assertContainsItem(index, FavouriteContent(content, false))
        }
        robot.assertDidNotNavigateToAuth()
    }

    // region a
    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived0() {
        System.err.println("test start")
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived1() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived2() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived3() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived4() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived5() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived6() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived7() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived8() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceived9() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }
    // endregion

    // region b
    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb0() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb1() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb2() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb3() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb4() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb5() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb6() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb7() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb8() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedb9() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }
    // endregion

    // region c
    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc0() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc1() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc2() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc3() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc4() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc5() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc6() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc7() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc8() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedc9() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }
    // endregion

    // region d
    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd0() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd1() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd2() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd3() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd4() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd5() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd6() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd7() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd8() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedd9() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }
    // endregion

    // region E
    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE0() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE1() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE2() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE3() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE4() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE5() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE6() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE7() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE8() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }

    /** GIVEN success then error WHEN retried THEN error is shown */
    @Test
    fun errorIsShownIfTheDataIsFetchedAndErrorIsReceivedE9() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.Error(usingRefreshedToken = false))
        )
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        System.err.println("test - before refresh")
        robot.swipeRefresh()
        System.err.println("test - after refresh")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - advanced")

        robot
            .assertContainsError()
            .assertContainsNoItems()
            .assertDidNotNavigateToAuth()
    }
    // endregion

    /** GIVEN unauthenticated then success WHEN loaded THEN success is shown */
    @Test
    @Ignore("a")
    fun authenticationIsHandledWithASingleLoading() {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Unauthorized(usingRefreshedToken = false)
                .then(ContentScenario.Success(usingRefreshedToken = true))
        )
            .setScenario(RefreshTokenScenario.Success)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        ContentData.contentSuccess.forEachIndexed { index, content ->
            robot.assertContainsItem(index, FavouriteContent(content, false))
        }
        robot.assertDidNotNavigateToAuth()
    }

    /** GIVEN unauthenticated then error WHEN loaded THEN navigated to auth */
    @Test
    @Ignore("a")
    fun sessionExpirationResultsInNavigation() {
        mockServerScenarioSetup.setScenario(ContentScenario.Unauthorized(usingRefreshedToken = false))
            .setScenario(RefreshTokenScenario.Error)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        System.err.println("test - calling first advanceUntilIdleWithIdlingResources")
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        System.err.println("test - finished first advanceUntilIdleWithIdlingResources")

        robot.assertNavigatedToAuth()
    }
}
