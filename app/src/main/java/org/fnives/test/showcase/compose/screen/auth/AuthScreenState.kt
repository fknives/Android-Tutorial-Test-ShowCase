package org.fnives.test.showcase.compose.screen.auth

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.ui.shared.Event
import org.koin.androidx.compose.get

@Composable
fun rememberAuthScreenState(
    stateScope: CoroutineScope = rememberCoroutineScope(),
    loginUseCase: LoginUseCase = get(),
): AuthScreenState {
    return remember { AuthScreenState(stateScope, loginUseCase) }
}

class AuthScreenState(
    private val stateScope: CoroutineScope,
    private val loginUseCase: LoginUseCase,
) {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<ErrorType?>(null)
        private set
    var navigateToHome by mutableStateOf<Event<Unit>?>(null)
        private set

    fun onUsernameChanged(username: String) {
        this.username = username
    }

    fun onPasswordChanged(password: String) {
        this.password = password
    }

    fun onLogin() {
        if (loading) {
            return
        }
        loading = true
        stateScope.launch {
            val credentials = LoginCredentials(
                username = username,
                password = password
            )
            when (val response = loginUseCase.invoke(credentials)) {
                is Answer.Error -> error = ErrorType.GENERAL_NETWORK_ERROR
                is Answer.Success -> processLoginStatus(response.data)
            }
            loading = false
        }
    }

    private fun processLoginStatus(loginStatus: LoginStatus) {
        when (loginStatus) {
            LoginStatus.SUCCESS -> navigateToHome = Event(Unit)
            LoginStatus.INVALID_CREDENTIALS -> error = ErrorType.INVALID_CREDENTIALS
            LoginStatus.INVALID_USERNAME -> error = ErrorType.UNSUPPORTED_USERNAME
            LoginStatus.INVALID_PASSWORD -> error = ErrorType.UNSUPPORTED_PASSWORD
        }
    }

    fun dismissError() {
        error = null
    }

    enum class ErrorType {
        INVALID_CREDENTIALS,
        GENERAL_NETWORK_ERROR,
        UNSUPPORTED_USERNAME,
        UNSUPPORTED_PASSWORD
    }
}