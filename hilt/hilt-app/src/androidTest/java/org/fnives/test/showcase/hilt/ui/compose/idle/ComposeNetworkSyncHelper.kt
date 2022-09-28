package org.fnives.test.showcase.hilt.ui.compose.idle

import android.util.Log
import androidx.compose.ui.test.junit4.ComposeTestRule
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.CompositeDisposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.Disposable
import org.fnives.test.showcase.hilt.network.testutil.NetworkSynchronization
import javax.inject.Inject

class ComposeNetworkSyncHelper @Inject constructor(
    private val networkSynchronization: NetworkSynchronization,
) {

    private var disposable: Disposable? = null

    fun setup(composeTestRule: ComposeTestRule) {
        disposable = networkSynchronization.networkIdlingResources()
            .map(::EspressoToComposeIdlingResourceAdapter)
            .map { ComposeIdlingDisposable(it, composeTestRule) }
            .let(::CompositeDisposable)
    }

    fun tearDown() {
        if (disposable == null) {
            Log.w("ComposeNetworkSyncHelper", "tearDown called, but setup wasn't!")
        }
        disposable?.dispose()
    }
}
