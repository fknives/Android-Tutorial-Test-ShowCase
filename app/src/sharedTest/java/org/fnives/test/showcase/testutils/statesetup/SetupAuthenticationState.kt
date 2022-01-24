package org.fnives.test.showcase.testutils.statesetup

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.testutils.configuration.MainDispatcherTestRule
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.HomeRobot
import org.fnives.test.showcase.ui.home.MainActivity
import org.fnives.test.showcase.ui.login.LoginRobot
import org.koin.test.KoinTest

object SetupAuthenticationState : KoinTest {

    fun setupLogin(
        mainDispatcherTestRule: MainDispatcherTestRule,
        mockServerScenarioSetup: MockServerScenarioSetup
    ) {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "a", password = "b"))
        val activityScenario = ActivityScenario.launch(AuthActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        val loginRobot = LoginRobot()
        loginRobot.setupIntentResults()
        loginRobot
            .setPassword("b")
            .setUsername("a")
            .clickOnLogin()

        mainDispatcherTestRule.advanceUntilIdleOrActivityIsDestroyed()

        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    fun setupLogout(
        mainDispatcherTestRule: MainDispatcherTestRule
    ) {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        val homeRobot = HomeRobot()
        homeRobot
            .clickSignOut()

        mainDispatcherTestRule.advanceUntilIdleOrActivityIsDestroyed()

        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }
}
