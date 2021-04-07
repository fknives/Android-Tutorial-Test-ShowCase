package org.fnives.test.showcase.network.auth

import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException

interface LoginRemoteSource {

    @Throws(NetworkException::class, ParsingException::class)
    suspend fun login(credentials: LoginCredentials): LoginStatusResponses
}
