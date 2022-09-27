package org.fnives.test.showcase.hilt.network.auth

import org.fnives.test.showcase.hilt.network.auth.model.CredentialsRequest
import org.fnives.test.showcase.hilt.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.hilt.network.shared.ExceptionWrapper
import org.fnives.test.showcase.hilt.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.hilt.network.shared.exceptions.ParsingException
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.session.Session
import javax.inject.Inject

internal class LoginRemoteSourceImpl @Inject internal constructor(
    private val loginService: LoginService,
    private val loginErrorConverter: LoginErrorConverter
) : LoginRemoteSource {

    @Throws(NetworkException::class, ParsingException::class)
    override suspend fun login(credentials: LoginCredentials): LoginStatusResponses =
        loginErrorConverter.invoke {
            loginService.login(CredentialsRequest(user = credentials.username, password = credentials.password))
        }

    @Throws(NetworkException::class, ParsingException::class)
    internal suspend fun refresh(refreshToken: String): Session = ExceptionWrapper.wrap {
        val response = loginService.refreshToken(refreshToken)
        Session(accessToken = response.accessToken, refreshToken = response.refreshToken)
    }
}
