package org.fnives.test.showcase.network.session

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.fnives.test.showcase.network.auth.LoginRemoteSourceImpl

internal class SessionAuthenticator(
    private val networkSessionLocalStorage: NetworkSessionLocalStorage,
    private val loginRemoteSource: LoginRemoteSourceImpl,
    private val authenticationHeaderUtils: AuthenticationHeaderUtils,
    private val networkSessionExpirationListener: NetworkSessionExpirationListener
) : Authenticator {

    @Suppress("SwallowedException")
    override fun authenticate(route: Route?, response: Response): Request? {
        if (authenticationHeaderUtils.hasToken(response.request)) {
            return runBlocking {
                try {
                    val refreshToken = networkSessionLocalStorage.session
                        ?.refreshToken
                        .orEmpty()
                    val newSession = loginRemoteSource.refresh(refreshToken)
                    networkSessionLocalStorage.session = newSession
                    return@runBlocking authenticationHeaderUtils.attachToken(response.request)
                } catch (throwable: Throwable) {
                    networkSessionLocalStorage.session = null
                    networkSessionExpirationListener.onSessionExpired()
                    return@runBlocking null
                }
            }
        } else {
            return authenticationHeaderUtils.attachToken(response.request)
        }
    }
}
