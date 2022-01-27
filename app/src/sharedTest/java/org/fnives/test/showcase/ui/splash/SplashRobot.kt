package org.fnives.test.showcase.ui.splash

import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.fnives.test.showcase.testutils.robot.Robot
import org.fnives.test.showcase.testutils.viewactions.notIntended
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.MainActivity

class SplashRobot : Robot {

    override fun init() {
        Intents.init()
    }

    override fun release() {
        Intents.release()
    }

    fun assertHomeIsStarted() = apply {
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.canonicalName))
    }

    fun assertHomeIsNotStarted() = apply {
        notIntended(IntentMatchers.hasComponent(MainActivity::class.java.canonicalName))
    }

    fun assertAuthIsStarted() = apply {
        Intents.intended(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
    }

    fun assertAuthIsNotStarted() = apply {
        notIntended(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
    }
}
