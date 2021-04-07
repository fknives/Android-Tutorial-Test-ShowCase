package org.fnives.test.showcase.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.ui.shared.Event

class AuthViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username
    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _error = MutableLiveData<Event<ErrorType>>()
    val error: LiveData<Event<ErrorType>> = _error
    private val _navigateToHome = MutableLiveData<Event<Unit>>()
    val navigateToHome: LiveData<Event<Unit>> = _navigateToHome

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun onLogin() {
        if (_loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            val credentials = LoginCredentials(
                username = _username.value.orEmpty(),
                password = _password.value.orEmpty()
            )
            when (val response = loginUseCase.invoke(credentials)) {
                is Answer.Error -> _error.value = Event(ErrorType.GENERAL_NETWORK_ERROR)
                is Answer.Success -> processLoginStatus(response.data)
            }
            _loading.postValue(false)
        }
    }

    private fun processLoginStatus(loginStatus: LoginStatus) {
        when (loginStatus) {
            LoginStatus.SUCCESS -> _navigateToHome.value = Event(Unit)
            LoginStatus.INVALID_CREDENTIALS -> _error.value = Event(ErrorType.INVALID_CREDENTIALS)
            LoginStatus.INVALID_USERNAME -> _error.value = Event(ErrorType.UNSUPPORTED_USERNAME)
            LoginStatus.INVALID_PASSWORD -> _error.value = Event(ErrorType.UNSUPPORTED_PASSWORD)
        }
    }

    enum class ErrorType {
        INVALID_CREDENTIALS,
        GENERAL_NETWORK_ERROR,
        UNSUPPORTED_USERNAME,
        UNSUPPORTED_PASSWORD
    }
}
