package org.fnives.test.showcase.ui.splash

import android.app.Instrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.fnives.test.showcase.testutils.robot.Robot
import org.fnives.test.showcase.testutils.viewactions.notIntended
import org.fnives.test.showcase.ui.ActivityClassHolder

class SplashRobot : Robot {

    override fun init() {
        Intents.init()
        Intents.intending(IntentMatchers.hasComponent(ActivityClassHolder.mainActivity().java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(0, null))
        Intents.intending(IntentMatchers.hasComponent(ActivityClassHolder.authActivity().java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(0, null))
    }

    override fun release() {
        Intents.release()
    }

    fun assertHomeIsStarted() = apply {
        Intents.intended(IntentMatchers.hasComponent(ActivityClassHolder.mainActivity().java.canonicalName))
    }

    fun assertHomeIsNotStarted() = apply {
        notIntended(IntentMatchers.hasComponent(ActivityClassHolder.mainActivity().java.canonicalName))
    }

    fun assertAuthIsStarted() = apply {
        Intents.intended(IntentMatchers.hasComponent(ActivityClassHolder.authActivity().java.canonicalName))
    }

    fun assertAuthIsNotStarted() = apply {
        notIntended(IntentMatchers.hasComponent(ActivityClassHolder.authActivity().java.canonicalName))
    }
}
