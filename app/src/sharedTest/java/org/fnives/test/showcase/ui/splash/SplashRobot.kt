package org.fnives.test.showcase.ui.splash

import android.app.Instrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.fnives.test.showcase.testutils.robot.Robot
import org.fnives.test.showcase.testutils.viewactions.notIntended
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.MainActivity

class SplashRobot : Robot {

    override fun init() {
        Intents.init()
        Intents.intending(IntentMatchers.hasComponent(MainActivity::class.java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(0, null))
        Intents.intending(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(0, null))
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
