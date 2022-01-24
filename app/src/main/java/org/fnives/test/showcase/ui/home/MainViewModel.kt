package org.fnives.test.showcase.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.fnives.test.showcase.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.core.content.FetchContentUseCase
import org.fnives.test.showcase.core.content.GetAllContentUseCase
import org.fnives.test.showcase.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.ui.shared.Event

class MainViewModel(
    private val getAllContentUseCase: GetAllContentUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val fetchContentUseCase: FetchContentUseCase,
    private val addContentToFavouriteUseCase: AddContentToFavouriteUseCase,
    private val removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _content: LiveData<List<FavouriteContent>> = liveData {
        getAllContentUseCase.get().collect {
            when (it) {
                is Resource.Error -> {
                    _errorMessage.value = true
                    _loading.value = false
                    emit(emptyList<FavouriteContent>())
                }
                is Resource.Loading -> {
                    _errorMessage.value = false
                    _loading.value = true
                }
                is Resource.Success -> {
                    _errorMessage.value = false
                    _loading.value = false
                    emit(it.data)
                }
            }
        }
    }
    val content: LiveData<List<FavouriteContent>> = _content
    private val _errorMessage = MutableLiveData<Boolean>(false)
    val errorMessage: LiveData<Boolean> = _errorMessage.distinctUntilChanged()
    private val _navigateToAuth = MutableLiveData<Event<Unit>>()
    val navigateToAuth: LiveData<Event<Unit>> = _navigateToAuth

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase.invoke()
            _navigateToAuth.value = Event(Unit)
        }
    }

    fun onRefresh() {
        if (_loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            fetchContentUseCase.invoke()
        }
    }

    fun onFavouriteToggleClicked(contentId: ContentId) {
        viewModelScope.launch {
            val content = _content.value?.firstOrNull { it.content.id == contentId } ?: return@launch
            if (content.isFavourite) {
                removeContentFromFavouritesUseCase.invoke(contentId)
            } else {
                addContentToFavouriteUseCase.invoke(contentId)
            }
        }
    }
}
