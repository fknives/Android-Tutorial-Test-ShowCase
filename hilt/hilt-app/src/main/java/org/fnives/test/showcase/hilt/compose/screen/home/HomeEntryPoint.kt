package org.fnives.test.showcase.hilt.compose.screen.home

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
import org.fnives.test.showcase.hilt.core.login.LogoutUseCase

object HomeEntryPoint {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MainDependencies {
        val getAllContentUseCase: GetAllContentUseCase
        val logoutUseCase: LogoutUseCase
        val fetchContentUseCase: FetchContentUseCase
        val addContentToFavouriteUseCase: AddContentToFavouriteUseCase
        val removeContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase
    }

    @Composable
    fun get(): MainDependencies {
        val context = LocalContext.current.applicationContext
        return remember { EntryPoints.get(context, MainDependencies::class.java) }
    }
}
