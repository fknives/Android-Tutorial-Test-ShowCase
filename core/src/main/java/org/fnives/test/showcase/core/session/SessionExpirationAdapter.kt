package org.fnives.test.showcase.core.session

import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener

internal class SessionExpirationAdapter(
    private val sessionExpirationListener: SessionExpirationListener
) : NetworkSessionExpirationListener {

    override fun onSessionExpired() = sessionExpirationListener.onSessionExpired()
}
