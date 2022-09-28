package org.fnives.test.showcase.hilt.network.auth

import org.fnives.test.showcase.hilt.network.auth.model.LoginResponse
import org.fnives.test.showcase.hilt.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.hilt.network.shared.ExceptionWrapper
import org.fnives.test.showcase.hilt.network.shared.exceptions.ParsingException
import org.fnives.test.showcase.model.session.Session
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

internal class LoginErrorConverter @Inject internal constructor() {

    @Throws(ParsingException::class)
    suspend fun invoke(request: suspend () -> Response<LoginResponse>): LoginStatusResponses =
        ExceptionWrapper.wrap {
            val response = request()
            if (response.code() == 400) {
                return@wrap LoginStatusResponses.InvalidCredentials
            } else if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val parsedResponse = try {
                response.body()!!
            } catch (nullPointerException: NullPointerException) {
                throw ParsingException(nullPointerException)
            }

            val session = Session(
                accessToken = parsedResponse.accessToken,
                refreshToken = parsedResponse.refreshToken
            )
            LoginStatusResponses.Success(session)
        }
}
