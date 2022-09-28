package org.fnives.test.showcase.hilt.core.login

import org.fnives.test.showcase.hilt.core.shared.wrapIntoAnswer
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.hilt.network.auth.LoginRemoteSource
import org.fnives.test.showcase.hilt.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.shared.Answer
import javax.inject.Inject

class LoginUseCase @Inject internal constructor(
    private val loginRemoteSource: LoginRemoteSource,
    private val userDataLocalStorage: UserDataLocalStorage,
) {

    suspend fun invoke(credentials: LoginCredentials): Answer<LoginStatus> {
        if (credentials.username.isBlank()) return Answer.Success(LoginStatus.INVALID_USERNAME)
        if (credentials.password.isBlank()) return Answer.Success(LoginStatus.INVALID_PASSWORD)

        return wrapIntoAnswer {
            when (val response = loginRemoteSource.login(credentials)) {
                LoginStatusResponses.InvalidCredentials -> LoginStatus.INVALID_CREDENTIALS
                is LoginStatusResponses.Success -> {
                    userDataLocalStorage.session = response.session
                    LoginStatus.SUCCESS
                }
            }
        }
    }
}
