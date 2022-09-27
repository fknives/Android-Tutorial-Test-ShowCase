package org.fnives.test.showcase.hilt.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.core.session.SessionExpirationListener
import org.fnives.test.showcase.hilt.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.hilt.session.SessionExpirationListenerImpl
import org.fnives.test.showcase.hilt.storage.LocalDatabase
import org.fnives.test.showcase.hilt.storage.SharedPreferencesManagerImpl
import org.fnives.test.showcase.hilt.storage.favourite.FavouriteContentLocalStorageImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    fun enableLogging(): Boolean = true

    @Singleton
    @Provides
    fun provideFavouriteDao(localDatabase: LocalDatabase) =
        localDatabase.favouriteDao

    @Provides
    fun provideSharedPreferencesManagerImpl(@ApplicationContext context: Context) =
        SharedPreferencesManagerImpl.create(context)

    @Provides
    fun provideFavouriteContentLocalStorage(
        favouriteContentLocalStorageImpl: FavouriteContentLocalStorageImpl
    ): FavouriteContentLocalStorage = favouriteContentLocalStorageImpl

    @Provides
    internal fun bindSessionExpirationListener(
        sessionExpirationListenerImpl: SessionExpirationListenerImpl
    ): SessionExpirationListener = sessionExpirationListenerImpl
}
