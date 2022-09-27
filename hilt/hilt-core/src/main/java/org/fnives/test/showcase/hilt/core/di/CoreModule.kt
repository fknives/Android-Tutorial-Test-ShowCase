package org.fnives.test.showcase.hilt.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.core.login.LogoutUseCase
import org.fnives.test.showcase.hilt.core.session.SessionExpirationAdapter
import org.fnives.test.showcase.hilt.core.storage.NetworkSessionLocalStorageAdapter
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.hilt.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.hilt.network.session.NetworkSessionLocalStorage

@InstallIn(SingletonComponent::class)
@Module
object CoreModule {

    @Provides
    internal fun bindNetworkSessionLocalStorageAdapter(
        networkSessionLocalStorageAdapter: NetworkSessionLocalStorageAdapter
    ): NetworkSessionLocalStorage = networkSessionLocalStorageAdapter

    @Provides
    internal fun bindNetworkSessionExpirationListener(
        sessionExpirationAdapter: SessionExpirationAdapter
    ): NetworkSessionExpirationListener = sessionExpirationAdapter

    @Provides
    fun provideLogoutUseCase(
        storage: UserDataLocalStorage,
        reloadLoggedInModuleInjectModule: ReloadLoggedInModuleInjectModule
    ): LogoutUseCase = LogoutUseCase(storage, reloadLoggedInModuleInjectModule)
}
