package org.fnives.test.showcase.hilt.network.auth

import org.fnives.test.showcase.hilt.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.hilt.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.hilt.network.shared.exceptions.ParsingException
import org.fnives.test.showcase.model.auth.LoginCredentials

interface LoginRemoteSource {

    @Throws(NetworkException::class, ParsingException::class)
    suspend fun login(credentials: LoginCredentials): LoginStatusResponses
}
