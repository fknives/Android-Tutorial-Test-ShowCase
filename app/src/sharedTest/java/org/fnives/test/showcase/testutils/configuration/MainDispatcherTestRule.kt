package org.fnives.test.showcase.testutils.configuration

import org.junit.rules.TestRule

interface MainDispatcherTestRule : TestRule {

    fun advanceUntilIdleWithIdlingResources()

    fun advanceUntilIdleOrActivityIsDestroyed()

    fun advanceUntilIdle()

    fun advanceTimeBy(delayInMillis: Long)
}
