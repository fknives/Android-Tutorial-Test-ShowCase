package org.fnives.test.showcase.android.testutil

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.fnives.test.showcase.android.testutil.StandardTestMainDispatcher.Companion.testDispatcher

/**
 * Custom Junit5 Extension which replaces the main dispatcher with a Standard [TestDispatcher]
 *
 * One can access the test dispatcher via [testDispatcher] static getter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StandardTestMainDispatcher : TestMainDispatcher() {

    override var createdTestDispatcher: TestDispatcher?
        get() = privateTestDispatcher
        set(value) {
            privateTestDispatcher = value
        }

    override fun createDispatcher(): TestDispatcher = StandardTestDispatcher()

    companion object {
        private var privateTestDispatcher: TestDispatcher? = null
        val testDispatcher: TestDispatcher
            get() = privateTestDispatcher
                ?: throw IllegalStateException("StandardTestMainDispatcher is in afterEach State")
    }
}
