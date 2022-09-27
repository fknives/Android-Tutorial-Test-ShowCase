package org.fnives.test.showcase.hilt.test.shared.testutils.idling

import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.CompositeDisposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.IdlingResourceDisposable
import org.fnives.test.showcase.hilt.network.testutil.NetworkSynchronization
import javax.inject.Inject

class NetworkSynchronizationHelper @Inject constructor(private val networkSynchronization: NetworkSynchronization) {

    private val compositeDisposable = CompositeDisposable()

    fun setup() {
        networkSynchronization.networkIdlingResources().map {
            IdlingResourceDisposable(it)
        }.forEach {
            compositeDisposable.add(it)
        }
    }

    fun dispose() {
        compositeDisposable.dispose()
    }
}
