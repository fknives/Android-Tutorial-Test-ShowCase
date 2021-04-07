package org.fnives.test.showcase.core.storage

import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage

internal class NetworkSessionLocalStorageAdapter(
    private val userDataLocalStorage: UserDataLocalStorage
) : NetworkSessionLocalStorage {

    override var session: Session?
        get() = userDataLocalStorage.session
        set(value) {
            userDataLocalStorage.session = value
        }
}
