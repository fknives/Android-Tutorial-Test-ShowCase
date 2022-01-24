package org.fnives.test.showcase.core.di.koin

import org.fnives.test.showcase.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.core.content.ContentRepository
import org.fnives.test.showcase.core.content.FetchContentUseCase
import org.fnives.test.showcase.core.content.GetAllContentUseCase
import org.fnives.test.showcase.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.core.session.SessionExpirationAdapter
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.core.storage.NetworkSessionLocalStorageAdapter
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.di.createNetworkModules
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun createCoreModule(
    baseUrl: BaseUrl,
    enableNetworkLogging: Boolean,
    userDataLocalStorageProvider: Scope.() -> UserDataLocalStorage,
    sessionExpirationListenerProvider: Scope.() -> SessionExpirationListener,
    favouriteContentLocalStorageProvider: Scope.() -> FavouriteContentLocalStorage
): Sequence<Module> =
    createNetworkModules(
        baseUrl = baseUrl,
        enableLogging = enableNetworkLogging,
        networkSessionLocalStorageProvider = { get<NetworkSessionLocalStorageAdapter>() },
        networkSessionExpirationListenerProvider = { SessionExpirationAdapter(sessionExpirationListenerProvider()) }
    )
        .plus(useCaseModule())
        .plus(storageModule(userDataLocalStorageProvider, favouriteContentLocalStorageProvider))
        .plus(repositoryModule())

fun repositoryModule() = module {
    single { ContentRepository(get()) }
}

fun useCaseModule() = module {
    factory { LoginUseCase(get(), get()) }
    factory { LogoutUseCase(get()) }
    factory { GetAllContentUseCase(get(), get()) }
    factory { AddContentToFavouriteUseCase(get()) }
    factory { RemoveContentFromFavouritesUseCase(get()) }
    factory { IsUserLoggedInUseCase(get()) }
    factory { FetchContentUseCase(get()) }
}

fun storageModule(
    userDataLocalStorageProvider: Scope.() -> UserDataLocalStorage,
    favouriteContentLocalStorageProvider: Scope.() -> FavouriteContentLocalStorage
) = module {
    single { userDataLocalStorageProvider() }
    single { favouriteContentLocalStorageProvider() }
    factory { NetworkSessionLocalStorageAdapter(get()) }
}
