package org.fnives.test.showcase.ui.compose.screen.home

import androidx.compose.runtime.*
import org.fnives.test.showcase.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.core.content.FetchContentUseCase
import org.fnives.test.showcase.core.content.GetAllContentUseCase
import org.fnives.test.showcase.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.koin.androidx.compose.get

@Composable
fun rememberHomeScreenState(
    getAllContentUseCase: GetAllContentUseCase = get(),
    logoutUseCase: LogoutUseCase = get(),
    fetchContentUseCase: FetchContentUseCase = get(),
    addContentToFavouriteUseCase: AddContentToFavouriteUseCase = get(),
    removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase = get(),
): HomeScreenState {
    return remember {
        HomeScreenState(
            getAllContentUseCase,
            logoutUseCase,
            fetchContentUseCase,
            addContentToFavouriteUseCase,
            removeContentFromFavouritesUseCase
        )
    }
}

class HomeScreenState(
    private val getAllContentUseCase: GetAllContentUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val fetchContentUseCase: FetchContentUseCase,
    private val addContentToFavouriteUseCase: AddContentToFavouriteUseCase,
    private val removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase
) {

    var loading by mutableStateOf(false)
        private set

}