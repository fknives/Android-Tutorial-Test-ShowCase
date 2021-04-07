package org.fnives.test.showcase.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.ui.shared.Event

class SplashViewModel(isUserLoggedInUseCase: IsUserLoggedInUseCase) : ViewModel() {

    private val _navigateTo = MutableLiveData<Event<NavigateTo>>()
    val navigateTo: LiveData<Event<NavigateTo>> = _navigateTo

    init {
        viewModelScope.launch {
            delay(500L)
            val navigationEvent = if (isUserLoggedInUseCase.invoke()) NavigateTo.HOME else NavigateTo.AUTHENTICATION
            _navigateTo.value = Event(navigationEvent)
        }
    }

    enum class NavigateTo {
        HOME, AUTHENTICATION
    }
}
