package org.fnives.test.showcase.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.ui.compose.ComposeActivity
import org.fnives.test.showcase.ui.compose.TestShowCaseApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class AuthComposeInstrumentedTest : KoinTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeActivity>()

//    private lateinit var activityScenario: ActivityScenario<ComposeActivity>

    private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
    private val mainDispatcherTestRule = MainDispatcherTestRule()
    private lateinit var robot: ComposeLoginRobot

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(mockServerScenarioSetupTestRule)
        .around(mainDispatcherTestRule)

    @Before
    fun setup() {
        robot = ComposeLoginRobot(composeTestRule)
        composeTestRule.setContent {
            TestShowCaseApp()
        }
    }

//    @After
//    fun tearDown() {
//        activityScenario.safeClose()
//    }

    /** GIVEN non empty password and username and successful response WHEN signIn THEN no error is shown and navigating to home */
    @Test
    fun properLoginResultsInNavigationToHome() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.Success(password = "alma", username = "banan")
        )
        composeTestRule.waitForIdle()
        robot
            .setPassword("alma")
            .setUsername("banan")
            .assertPassword("alma")
            .assertUsername("banan")
            .clickOnLogin()
//            .assertLoadingBeforeRequests()

        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
//        robot.assertNavigatedToHome()
    }

}