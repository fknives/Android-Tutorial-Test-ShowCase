package org.fnives.test.showcase.hilt.core.storage

import org.fnives.test.showcase.hilt.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.model.session.Session
import javax.inject.Inject

internal class NetworkSessionLocalStorageAdapter @Inject constructor(
    private val userDataLocalStorage: UserDataLocalStorage,
) : NetworkSessionLocalStorage {

    override var session: Session?
        get() = userDataLocalStorage.session
        set(value) {
            userDataLocalStorage.session = value
        }
}
