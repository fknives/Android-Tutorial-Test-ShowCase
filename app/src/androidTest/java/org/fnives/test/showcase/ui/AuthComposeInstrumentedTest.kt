package org.fnives.test.showcase.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.compose.ComposeActivity
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.testutils.idling.anyResourceIdling
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
    }

    /** GIVEN non empty password and username and successful response WHEN signIn THEN no error is shown and navigating to home */
    @Test
    fun properLoginResultsInNavigationToHome() {
        mockServerScenarioSetup.setScenario(
            AuthScenario.Success(password = "alma", username = "banan")
        )
        composeTestRule.mainClock.advanceTimeBy(500L)
        composeTestRule.mainClock.advanceTimeUntil { anyResourceIdling() }
        composeTestRule.waitForIdle()
        robot
            .setPassword("alma")
            .setUsername("banan")
            .assertUsername("banan")
            .assertPassword("alma")
        composeTestRule.mainClock.autoAdvance = false
        robot.clickOnLogin()
        composeTestRule.mainClock.advanceTimeByFrame()
        robot.assertLoading()

//        mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
//        robot.assertNavigatedToHome()
    }

}