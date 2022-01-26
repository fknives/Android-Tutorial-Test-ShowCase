package org.fnives.test.showcase.testutils

import android.app.Activity
import androidx.test.core.app.ActivityScenario

fun <T: Activity> ActivityScenario<T>.safeClose() {
    workaroundForActivityScenarioCLoseLockingUp()
    close()
}

/**
 * This should not be needed, we shouldn't use sleep ever.
 * However, it seems to be and issue described here: https://github.com/android/android-test/issues/676
 *
 * If an activity is finished in code, the ActivityScenario.close() can hang 30 to 45 seconds.
 * This sleeps let's the Activity finish it state change and unlocks the ActivityScenario.
 *
 * As soon as that issue is closed, this should be removed as well.
 */
private fun workaroundForActivityScenarioCLoseLockingUp() {
    Thread.sleep(1000L)
}