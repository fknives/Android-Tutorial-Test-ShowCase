package org.fnives.test.showcase.hilt.compose.screen.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.core.login.LoginUseCase

object AuthEntryPoint {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AuthDependencies {
        val loginUseCase: LoginUseCase
    }

    @Composable
    fun get(): AuthDependencies {
        val context = LocalContext.current.applicationContext
        return remember { EntryPoints.get(context, AuthDependencies::class.java) }
    }
}
