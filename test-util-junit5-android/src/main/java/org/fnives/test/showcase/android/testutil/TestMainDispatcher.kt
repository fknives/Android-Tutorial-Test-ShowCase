package org.fnives.test.showcase.android.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Custom Junit5 Extension which replaces the main dispatcher with a [TestDispatcher]
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class TestMainDispatcher : BeforeEachCallback, AfterEachCallback {

    protected abstract var createdTestDispatcher: TestDispatcher?

    abstract fun createDispatcher(): TestDispatcher

    final override fun beforeEach(context: ExtensionContext?) {
        val testDispatcher = createDispatcher()
        createdTestDispatcher = testDispatcher
        Dispatchers.setMain(testDispatcher)
    }

    final override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
        createdTestDispatcher = null
    }
}
