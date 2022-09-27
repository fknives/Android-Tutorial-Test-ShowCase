package org.fnives.test.showcase.hilt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.hilt.storage.SharedPreferencesManagerImpl
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [UserDataLocalStorageModule::class]
)
object TestUserDataLocalStorageModule {

    var replacement: UserDataLocalStorage? = null

    @Singleton
    @Provides
    fun provideUserDataLocalStorage(
        sharedPreferencesManagerImpl: SharedPreferencesManagerImpl,
    ): UserDataLocalStorage = replacement ?: sharedPreferencesManagerImpl
}
