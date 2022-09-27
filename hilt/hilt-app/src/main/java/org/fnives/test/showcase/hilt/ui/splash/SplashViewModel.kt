package org.fnives.test.showcase.hilt.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fnives.test.showcase.hilt.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.hilt.ui.shared.Event
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(isUserLoggedInUseCase: IsUserLoggedInUseCase) : ViewModel() {

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
