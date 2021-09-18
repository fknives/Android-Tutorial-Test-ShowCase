package org.fnives.test.showcase.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.session.SessionExpirationListenerImpl
import org.fnives.test.showcase.storage.SharedPreferencesManagerImpl
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.fnives.test.showcase.storage.favourite.FavouriteContentLocalStorageImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    fun provideBaseUrl(): String = BaseUrlProvider.get().baseUrl

    @Provides
    fun enableLogging(): Boolean = true

    @Singleton
    @Provides
    fun provideFavouriteDao(@ApplicationContext context: Context) =
        DatabaseInitialization.create(context).favouriteDao

    @Provides
    fun provideSharedPreferencesManagerImpl(@ApplicationContext context: Context) =
        SharedPreferencesManagerImpl.create(context)

    @Singleton
    @Provides
    fun provideUserDataLocalStorage(
        sharedPreferencesManagerImpl: SharedPreferencesManagerImpl
    ): UserDataLocalStorage = sharedPreferencesManagerImpl

    @Provides
    fun provideFavouriteContentLocalStorage(
        favouriteContentLocalStorageImpl: FavouriteContentLocalStorageImpl
    ): FavouriteContentLocalStorage = favouriteContentLocalStorageImpl

    @Provides
    internal fun bindSessionExpirationListener(
        sessionExpirationListenerImpl: SessionExpirationListenerImpl
    ) : SessionExpirationListener = sessionExpirationListenerImpl

}