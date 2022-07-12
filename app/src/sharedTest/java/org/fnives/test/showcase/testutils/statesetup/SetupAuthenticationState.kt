package org.fnives.test.showcase.testutils.statesetup

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.runner.intent.IntentStubberRegistry
import org.fnives.test.showcase.android.testutil.activity.safeClose
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.testutils.idling.MainDispatcherTestRule
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.HomeRobot
import org.fnives.test.showcase.ui.home.MainActivity
import org.fnives.test.showcase.ui.login.LoginRobot
import org.koin.test.KoinTest

object SetupAuthenticationState : KoinTest {

    fun setupLogin(
        mainDispatcherTestRule: MainDispatcherTestRule,
        mockServerScenarioSetup: MockServerScenarioSetup,
        resetIntents: Boolean = true,
    ) {
        resetIntentsIfNeeded(resetIntents) {
            mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "a", password = "b"))
            val activityScenario = ActivityScenario.launch(AuthActivity::class.java)
            activityScenario.moveToState(Lifecycle.State.RESUMED)

            val loginRobot = LoginRobot()
            loginRobot.setupIntentResults()
            loginRobot
                .setPassword("b")
                .setUsername("a")
                .clickOnLogin()
            mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

            activityScenario.safeClose()
        }
    }

    fun setupLogout(
        mainDispatcherTestRule: MainDispatcherTestRule,
        resetIntents: Boolean = true,
    ) {
        resetIntentsIfNeeded(resetIntents) {
            val activityScenario = ActivityScenario.launch(MainActivity::class.java)
            activityScenario.moveToState(Lifecycle.State.RESUMED)

            val homeRobot = HomeRobot()
            homeRobot.setupIntentResults()
            homeRobot.clickSignOut()
            mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

            activityScenario.safeClose()
        }
    }

    private fun resetIntentsIfNeeded(resetIntents: Boolean, action: () -> Unit) {
        val wasInitialized = IntentStubberRegistry.isLoaded()
        if (!wasInitialized) {
            Intents.init()
        }
        action()
        Intents.release()
        if (resetIntents && wasInitialized) {
            Intents.init()
        }
    }
}
