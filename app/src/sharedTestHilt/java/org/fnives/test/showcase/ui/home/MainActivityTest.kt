package org.fnives.test.showcase.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
import org.fnives.test.showcase.testutils.statesetup.SetupLoggedInState
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainActivityTest {

    private lateinit var activityScenario: ActivityScenario<HiltMainActivity>

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val snackbarVerificationTestRule = SpecificTestConfigurationsFactory.createSnackbarVerification()

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
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var setupLoggedInState: SetupLoggedInState

    @Inject
    lateinit var networkSynchronization: NetworkSynchronization

    private lateinit var disposable: Disposable

    @Before
    fun setUp() {
        SpecificTestConfigurationsFactory.createServerTypeConfiguration()
            .invoke(mockServerScenarioSetupTestRule.mockServerScenarioSetup)

        hiltRule.inject()
        setupLoggedInState.setupLogin(mockServerScenarioSetupTestRule.mockServerScenarioSetup)
        disposable = networkSynchronization.registerNetworkingSynchronization()
    }

    @After
    fun tearDown() {
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        disposable.dispose()
    }

    @Test
    fun GIVEN_initialized_MainActivity_WHEN_signout_is_clicked_THEN_user_is_signed_out() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Error(false))
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.clickSignOut()
        mainDispatcherTestRule.advanceUntilIdleOrActivityIsDestroyed()

        homeRobot.assertNavigatedToAuth()
        Assert.assertEquals(false, setupLoggedInState.isLoggedIn())
    }

    @Test
    fun GIVEN_success_response_WHEN_data_is_returned_THEN_it_is_shown_on_the_ui() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        ContentData.contentSuccess.forEach {
            homeRobot.assertContainsItem(FavouriteContent(it, false))
        }
        homeRobot.assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_success_response_WHEN_item_is_clicked_THEN_ui_is_updated() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)
        homeRobot.assertContainsItem(expectedItem)
            .assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_success_response_WHEN_item_is_clicked_THEN_ui_is_updated_even_if_activity_is_recreated() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), true)

        activityScenario.moveToState(Lifecycle.State.DESTROYED)
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertContainsItem(expectedItem)
            .assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_success_response_WHEN_item_is_clicked_then_clicked_again_THEN_ui_is_updated() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Success(false))
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        homeRobot.clickOnContentItem(ContentData.contentSuccess.first())
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        val expectedItem = FavouriteContent(ContentData.contentSuccess.first(), false)
        homeRobot.assertContainsItem(expectedItem)
            .assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_error_response_WHEN_loaded_THEN_error_is_Shown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Error(false))
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertContainsNoItems()
            .assertContainsError()
            .assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_error_response_then_success_WHEN_retried_THEN_success_is_shown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(
                ContentScenario.Error(false)
                    .then(ContentScenario.Success(false))
            )
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.swipeRefresh()
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
        loopMainThreadFor(2000L)

        ContentData.contentSuccess.forEach {
            homeRobot.assertContainsItem(FavouriteContent(it, false))
        }
        homeRobot.assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_success_then_error_WHEN_retried_THEN_error_is_shown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(
                ContentScenario.Success(false)
                    .then(ContentScenario.Error(false))
            )
        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
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

    @Test
    fun GIVEN_unauthenticated_then_success_WHEN_loaded_THEN_success_is_shown() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(
                ContentScenario.Unauthorized(false)
                    .then(ContentScenario.Success(true))
            )
            .setScenario(RefreshTokenScenario.Success)

        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        ContentData.contentSuccess.forEach {
            homeRobot.assertContainsItem(FavouriteContent(it, false))
        }
        homeRobot.assertDidNotNavigateToAuth()
    }

    @Test
    fun GIVEN_unauthenticated_then_error_WHEN_loaded_THEN_navigated_to_auth() {
        mockServerScenarioSetupTestRule.mockServerScenarioSetup
            .setScenario(ContentScenario.Unauthorized(false))
            .setScenario(RefreshTokenScenario.Error)

        activityScenario = ActivityScenario.launch(HiltMainActivity::class.java)
        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

        homeRobot.assertNavigatedToAuth()
        Assert.assertEquals(false, setupLoggedInState.isLoggedIn())
    }
}
