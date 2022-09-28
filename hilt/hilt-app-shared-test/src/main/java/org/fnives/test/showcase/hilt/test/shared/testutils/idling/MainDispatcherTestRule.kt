package org.fnives.test.showcase.hilt.test.shared.testutils.idling

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import org.fnives.test.showcase.hilt.test.shared.testutils.storage.TestDatabaseInitialization
import org.fnives.test.showcase.android.testutil.synchronization.MainDispatcherTestRule as LibMainDispatcherTestRule

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherTestRule(useStandard: Boolean = true) : LibMainDispatcherTestRule(useStandard) {

    override fun onTestDispatcherInitialized(testDispatcher: TestDispatcher) {
        TestDatabaseInitialization.dispatcher = testDispatcher
    }
}
