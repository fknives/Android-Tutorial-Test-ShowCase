package org.fnives.test.showcase.core.session

import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import javax.inject.Inject

internal class SessionExpirationAdapter @Inject constructor(
    private val sessionExpirationListener: SessionExpirationListener
) : NetworkSessionExpirationListener {

    override fun onSessionExpired() = sessionExpirationListener.onSessionExpired()
}
