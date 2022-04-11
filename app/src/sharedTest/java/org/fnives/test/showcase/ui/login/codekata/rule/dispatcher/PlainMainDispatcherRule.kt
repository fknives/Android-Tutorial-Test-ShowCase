package org.fnives.test.showcase.ui.login.codekata.rule.dispatcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Sets up the Dispatcher as Main and as the [DatabaseInitialization]'s dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlainMainDispatcherRule(private val useStandard: Boolean = true) : TestRule {

    private var _testDispatcher: TestDispatcher? = null
    val testDispatcher
        get() = _testDispatcher
            ?: throw IllegalStateException("TestDispatcher is accessed before it is initialized!")

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            try {
                val dispatcher = if (useStandard) StandardTestDispatcher() else UnconfinedTestDispatcher()
                Dispatchers.setMain(dispatcher)
                DatabaseInitialization.dispatcher = dispatcher
                _testDispatcher = dispatcher
                base.evaluate()
            } finally {
                _testDispatcher = null
                Dispatchers.resetMain()
            }
        }
    }
}
