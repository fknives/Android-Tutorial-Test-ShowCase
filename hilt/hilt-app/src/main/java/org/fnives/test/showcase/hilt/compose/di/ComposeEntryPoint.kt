package org.fnives.test.showcase.hilt.compose.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.hilt.core.content.FetchContentUseCase
import org.fnives.test.showcase.hilt.core.content.GetAllContentUseCase
import org.fnives.test.showcase.hilt.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.hilt.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.hilt.core.login.LoginUseCase
import org.fnives.test.showcase.hilt.core.login.LogoutUseCase

object ComposeEntryPoint {

    /**
     * Helper method to easily remember and access Hilt Dependencies in Compose.
     */
    @Composable
    inline fun <reified T : Any> rememberEntryPoint(component: Any = LocalContext.current.applicationContext): T =
        remember(component) { EntryPoints.get(component, T::class.java) }

    sealed interface EntryPointDependencies

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AuthDependencies : EntryPointDependencies {
        val loginUseCase: LoginUseCase
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MainDependencies : EntryPointDependencies {
        val getAllContentUseCase: GetAllContentUseCase
        val logoutUseCase: LogoutUseCase
        val fetchContentUseCase: FetchContentUseCase
        val addContentToFavouriteUseCase: AddContentToFavouriteUseCase
        val removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppNavigationDependencies : EntryPointDependencies {
        val isUserLoggedInUseCase: IsUserLoggedInUseCase
    }
}
