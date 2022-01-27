package org.fnives.test.showcase.di

import org.fnives.test.showcase.core.di.createCoreModule
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.session.SessionExpirationListenerImpl
import org.fnives.test.showcase.storage.LocalDatabase
import org.fnives.test.showcase.storage.SharedPreferencesManagerImpl
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.fnives.test.showcase.storage.favourite.FavouriteContentLocalStorageImpl
import org.fnives.test.showcase.ui.auth.AuthViewModel
import org.fnives.test.showcase.ui.home.MainViewModel
import org.fnives.test.showcase.ui.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun createAppModules(baseUrl: BaseUrl): List<Module> {
    return createCoreModule(
        baseUrl = baseUrl,
        true,
        userDataLocalStorageProvider = { get<SharedPreferencesManagerImpl>() },
        sessionExpirationListenerProvider = { get<SessionExpirationListenerImpl>() },
        favouriteContentLocalStorageProvider = { get<FavouriteContentLocalStorageImpl>() }
    )
        .plus(storageModule())
        .plus(authModule())
        .plus(appModule())
        .plus(favouriteModule())
        .plus(splashModule())
        .toList()
}

fun storageModule() = module {
    single { SharedPreferencesManagerImpl.create(androidContext()) }
    single { DatabaseInitialization.create(androidContext()) }
}

fun authModule() = module {
    viewModel { AuthViewModel(get()) }
}

fun appModule() = module {
    single { SessionExpirationListenerImpl(androidContext()) }
}

fun splashModule() = module {
    viewModel { SplashViewModel(get()) }
}

fun favouriteModule() = module {
    single { get<LocalDatabase>().favouriteDao }
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
    single { FavouriteContentLocalStorageImpl(get()) }
}
