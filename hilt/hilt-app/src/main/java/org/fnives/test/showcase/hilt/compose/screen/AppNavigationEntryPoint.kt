package org.fnives.test.showcase.hilt.compose.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.core.login.IsUserLoggedInUseCase

object AppNavigationEntryPoint {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppNavigationDependencies {
        val isUserLoggedInUseCase: IsUserLoggedInUseCase
    }

    @Composable
    fun get(): AppNavigationDependencies {
        val context = LocalContext.current.applicationContext
        return remember { EntryPoints.get(context, AppNavigationDependencies::class.java) }
    }
}
