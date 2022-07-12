package org.fnives.test.showcase.android.testutil

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.fnives.test.showcase.android.testutil.UnconfinedTestMainDispatcher.Companion.testDispatcher

/**
 * Custom Junit5 Extension which replaces the main dispatcher with a Unconfined [TestDispatcher]
 *
 * One can access the test dispatcher via [testDispatcher] static getter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UnconfinedTestMainDispatcher : TestMainDispatcher() {

    override var createdTestDispatcher: TestDispatcher?
        get() = privateTestDispatcher
        set(value) {
            privateTestDispatcher = value
        }

    override fun createDispatcher(): TestDispatcher = UnconfinedTestDispatcher()

    companion object {
        private var privateTestDispatcher: TestDispatcher? = null
        val testDispatcher: TestDispatcher
            get() = privateTestDispatcher
                ?: throw IllegalStateException("StandardTestMainDispatcher is in afterEach State")
    }
}
