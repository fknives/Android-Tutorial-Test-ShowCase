package org.fnives.test.showcase.hilt.compose.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.fnives.test.showcase.hilt.compose.di.ComposeEntryPoint.MainDependencies
import org.fnives.test.showcase.hilt.compose.di.ComposeEntryPoint.rememberEntryPoint
import org.fnives.test.showcase.hilt.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.hilt.core.content.FetchContentUseCase
import org.fnives.test.showcase.hilt.core.content.GetAllContentUseCase
import org.fnives.test.showcase.hilt.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.hilt.core.login.LogoutUseCase
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.shared.Resource

@Composable
fun rememberHomeScreenState(
    stateScope: CoroutineScope = rememberCoroutineScope(),
    mainDependencies: MainDependencies = rememberEntryPoint(),
    onLogout: () -> Unit = {},
) =
    rememberHomeScreenState(
        stateScope = stateScope,
        getAllContentUseCase = mainDependencies.getAllContentUseCase,
        logoutUseCase = mainDependencies.logoutUseCase,
        fetchContentUseCase = mainDependencies.fetchContentUseCase,
        addContentToFavouriteUseCase = mainDependencies.addContentToFavouriteUseCase,
        removeContentFromFavouritesUseCase = mainDependencies.removeContentFromFavouritesUseCase,
        onLogout = onLogout,
    )

@Suppress("LongParameterList")
@Composable
fun rememberHomeScreenState(
    stateScope: CoroutineScope = rememberCoroutineScope(),
    getAllContentUseCase: GetAllContentUseCase,
    logoutUseCase: LogoutUseCase,
    fetchContentUseCase: FetchContentUseCase,
    addContentToFavouriteUseCase: AddContentToFavouriteUseCase,
    removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase,
    onLogout: () -> Unit = {},
): HomeScreenState {
    return remember {
        HomeScreenState(
            stateScope,
            getAllContentUseCase,
            logoutUseCase,
            fetchContentUseCase,
            addContentToFavouriteUseCase,
            removeContentFromFavouritesUseCase,
            onLogout,
        )
    }
}

@Suppress("LongParameterList")
class HomeScreenState(
    private val stateScope: CoroutineScope,
    private val getAllContentUseCase: GetAllContentUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val fetchContentUseCase: FetchContentUseCase,
    private val addContentToFavouriteUseCase: AddContentToFavouriteUseCase,
    private val removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase,
    private val logoutEvent: () -> Unit,
) {

    var loading by mutableStateOf(false)
        private set
    var isError by mutableStateOf(false)
        private set
    var content by mutableStateOf<List<FavouriteContent>>(emptyList())
        private set

    init {
        stateScope.launch {
            fetch().collect {
                content = it
            }
        }
    }

    private fun fetch() = getAllContentUseCase.get()
        .mapNotNull {
            when (it) {
                is Resource.Error -> {
                    isError = true
                    loading = false
                    return@mapNotNull emptyList<FavouriteContent>()
                }
                is Resource.Loading -> {
                    isError = false
                    loading = true
                    return@mapNotNull null
                }
                is Resource.Success -> {
                    isError = false
                    loading = false
                    return@mapNotNull it.data
                }
            }
        }

    fun onLogout() {
        stateScope.launch {
            logoutUseCase.invoke()
            logoutEvent()
        }
    }

    fun onRefresh() {
        if (loading) return
        loading = true
        stateScope.launch {
            fetchContentUseCase.invoke()
        }
    }

    fun onFavouriteToggleClicked(contentId: ContentId) {
        stateScope.launch {
            val item = content.firstOrNull { it.content.id == contentId } ?: return@launch
            if (item.isFavourite) {
                removeContentFromFavouritesUseCase.invoke(contentId)
            } else {
                addContentToFavouriteUseCase.invoke(contentId)
            }
        }
    }
}
