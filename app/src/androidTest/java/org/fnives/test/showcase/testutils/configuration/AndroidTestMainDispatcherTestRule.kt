package org.fnives.test.showcase.testutils.configuration

import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import kotlinx.coroutines.Dispatchers
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.fnives.test.showcase.testutils.idling.loopMainThreadFor
import org.fnives.test.showcase.testutils.idling.loopMainThreadUntilIdleWithIdlingResources
import org.junit.runner.Description
import org.junit.runners.model.Statement

class AndroidTestMainDispatcherTestRule : MainDispatcherTestRule {

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                DatabaseInitialization.dispatcher = Dispatchers.Main
                base.evaluate()
            }
        }

    override fun advanceUntilIdleWithIdlingResources() {
        loopMainThreadUntilIdleWithIdlingResources()
    }

    override fun advanceUntilIdleOrActivityIsDestroyed() {
        try {
            advanceUntilIdleWithIdlingResources()
            Espresso.onView(ViewMatchers.isRoot()).check(ViewAssertions.doesNotExist())
        } catch (noActivityResumedException: NoActivityResumedException) {
            // expected to happen
        } catch (runtimeException: RuntimeException) {
            if (runtimeException.message?.contains("No activities found") == true) {
                // expected to happen
            } else {
                throw runtimeException
            }
        }
    }

    override fun advanceUntilIdle() {
        loopMainThreadUntilIdleWithIdlingResources()
    }

    override fun advanceTimeBy(delayInMillis: Long) {
        loopMainThreadFor(delayInMillis)
    }
}
