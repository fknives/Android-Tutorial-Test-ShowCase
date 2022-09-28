package org.fnives.test.showcase.hilt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.hilt.storage.SharedPreferencesManagerImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UserDataLocalStorageModule {

    @Singleton
    @Provides
    fun provideUserDataLocalStorage(
        sharedPreferencesManagerImpl: SharedPreferencesManagerImpl
    ): UserDataLocalStorage = sharedPreferencesManagerImpl
}
