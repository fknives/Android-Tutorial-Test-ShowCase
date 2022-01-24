package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.shared.wrapIntoAnswer
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.network.auth.LoginRemoteSource
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses

class LoginUseCase internal constructor(
    private val loginRemoteSource: LoginRemoteSource,
    private val userDataLocalStorage: UserDataLocalStorage
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
