package org.fnives.test.showcase.di

import android.content.Context
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.android.testutil.StandardTestMainDispatcher
import org.fnives.test.showcase.ui.auth.AuthViewModel
import org.fnives.test.showcase.ui.home.MainViewModel
import org.fnives.test.showcase.ui.splash.SplashViewModel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.inject
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(StandardTestMainDispatcher::class)
class DITest : KoinTest {

    private val authViewModel by inject<AuthViewModel>()
    private val mainViewModel by inject<MainViewModel>()
    private val splashViewModel by inject<SplashViewModel>()

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyStaticModules() {
        val mockContext = mock<Context>()
        whenever(mockContext.getSharedPreferences(anyOrNull(), anyOrNull())).doReturn(mock())
        checkModules {
            androidContext(mockContext)
            modules(createAppModules(BaseUrl("https://a.com/")))
        }
    }

    @Test
    fun verifyViewModelModules() {
        val mockContext = mock<Context>()
        whenever(mockContext.getSharedPreferences(anyOrNull(), anyOrNull())).doReturn(mock())
        startKoin {
            androidContext(mockContext)
            modules(createAppModules(BaseUrl("https://a.com/")))
        }
        authViewModel
        mainViewModel
        splashViewModel
    }
}
