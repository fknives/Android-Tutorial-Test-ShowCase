package org.fnives.test.showcase.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.fnives.test.showcase.testutils.TestMainDispatcher.Companion.testDispatcher
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Custom Junit5 Extension which replaces the [DatabaseInitialization]'s dispatcher and main dispatcher with a [TestCoroutineDispatcher]
 *
 * One can access the test dispatcher via [testDispatcher] static getter.
 */
class TestMainDispatcher : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        val testDispatcher = TestCoroutineDispatcher()
        privateTestDispatcher = testDispatcher
        testDispatcher.pauseDispatcher()
        DatabaseInitialization.dispatcher = testDispatcher
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        privateTestDispatcher = null
    }

    companion object {
        private var privateTestDispatcher: TestCoroutineDispatcher? = null
        val testDispatcher: TestCoroutineDispatcher
            get() = privateTestDispatcher ?: throw IllegalStateException("TestMainDispatcher is in afterEach State")
    }
}
