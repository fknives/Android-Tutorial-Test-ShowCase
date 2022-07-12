package org.fnives.test.showcase.android.testutil.activity

import android.app.Activity
import androidx.test.core.app.ActivityScenario

/**
 * Workaround for issue: https://github.com/android/android-test/issues/676.
 *
 * Call this instead of ActivityScenario.close().
 */
fun <T : Activity> ActivityScenario<T>.safeClose() {
    workaroundForActivityScenarioCLoseLockingUp()
    close()
}

/**
 * This should not be needed, we shouldn't use sleep basically ever.
 * However, it seems to be and issue described here: https://github.com/android/android-test/issues/676
 *
 * If an activity is finished in code, the ActivityScenario.close() can hang 30 to 45 seconds.
 * This sleep let's the Activity finish it's state change and unlocks the ActivityScenario.
 *
 * As soon as that issue is closed, this should be removed as well.
 */
private fun workaroundForActivityScenarioCLoseLockingUp() {
    Thread.sleep(1000L)
}
