package org.fnives.test.showcase.hilt.compose.screen.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.fnives.test.showcase.hilt.core.login.LoginUseCase
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.shared.Answer

@Composable
fun rememberAuthScreenState(
    stateScope: CoroutineScope = rememberCoroutineScope { Dispatchers.Main },
    loginUseCase: LoginUseCase = AuthEntryPoint.get().loginUseCase,
    onLoginSuccess: () -> Unit = {},
): AuthScreenState {
    return rememberSaveable(saver = AuthScreenState.getSaver(stateScope, loginUseCase, onLoginSuccess)) {
        AuthScreenState(stateScope, loginUseCase, onLoginSuccess)
    }
}

class AuthScreenState(
    private val stateScope: CoroutineScope,
    private val loginUseCase: LoginUseCase,
    private val onLoginSuccess: () -> Unit = {},
) {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<ErrorType?>(null)
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
            LoginStatus.SUCCESS -> onLoginSuccess()
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

    companion object {
        private const val USERNAME = "USERNAME"
        private const val PASSWORD = "PASSWORD"

        fun getSaver(
            stateScope: CoroutineScope,
            loginUseCase: LoginUseCase,
            onLoginSuccess: () -> Unit,
        ): Saver<AuthScreenState, *> = mapSaver(
            save = { mapOf(USERNAME to it.username, PASSWORD to it.password) },
            restore = {
                AuthScreenState(stateScope, loginUseCase, onLoginSuccess).apply {
                    onUsernameChanged(it.getOrElse(USERNAME) { "" } as String)
                    onPasswordChanged(it.getOrElse(PASSWORD) { "" } as String)
                }
            }
        )
    }
}
