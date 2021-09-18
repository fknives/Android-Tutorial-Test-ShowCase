package org.fnives.test.showcase.core.di.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.core.session.SessionExpirationAdapter
import org.fnives.test.showcase.core.storage.NetworkSessionLocalStorageAdapter
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.core.di.hilt.ReloadLoggedInModuleInjectModule

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
    ) : LogoutUseCase = LogoutUseCase(storage, reloadLoggedInModuleInjectModule)
}