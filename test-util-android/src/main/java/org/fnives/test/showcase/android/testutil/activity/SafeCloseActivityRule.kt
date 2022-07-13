package org.fnives.test.showcase.android.testutil.activity

import androidx.test.core.app.ActivityScenario
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Test Rule which closes the given [scenario] safely when the Test is finished.
 */
class SafeCloseActivityRule(val scenario: () -> ActivityScenario<*>) : TestWatcher() {

    override fun finished(description: Description) {
        super.finished(description)
        scenario().safeClose()
    }
}
