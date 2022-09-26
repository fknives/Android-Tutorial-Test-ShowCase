package org.fnives.test.showcase.ui.splash

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.fnives.test.showcase.android.testutil.intent.notIntended
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.MainActivity

class SplashRobot {

    fun setupIntentResults() {
        Intents.intending(IntentMatchers.hasComponent(MainActivity::class.java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
        Intents.intending(IntentMatchers.hasComponent(AuthActivity::class.java.canonicalName))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
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
