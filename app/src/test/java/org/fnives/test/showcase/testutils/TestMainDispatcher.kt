package org.fnives.test.showcase.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.testutils.TestMainDispatcher.Companion.testDispatcher
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Custom Junit5 Extension which replaces the main dispatcher with a [TestDispatcher]
 *
 * One can access the test dispatcher via [testDispatcher] static getter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestMainDispatcher : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        val testDispatcher = StandardTestDispatcher()
        privateTestDispatcher = testDispatcher
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        privateTestDispatcher = null
    }

    companion object {
        private var privateTestDispatcher: TestDispatcher? = null
        val testDispatcher: TestDispatcher
            get() = privateTestDispatcher
                ?: throw IllegalStateException("TestMainDispatcher is in afterEach State")
    }
}
