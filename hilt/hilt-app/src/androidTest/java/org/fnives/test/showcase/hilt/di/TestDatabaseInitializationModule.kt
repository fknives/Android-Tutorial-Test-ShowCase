package org.fnives.test.showcase.hilt.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.fnives.test.showcase.hilt.storage.LocalDatabase
import org.fnives.test.showcase.hilt.test.shared.testutils.storage.TestDatabaseInitialization
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [StorageModule::class]
)
object TestDatabaseInitializationModule {

    @Singleton
    @Provides
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase =
        TestDatabaseInitialization.provideLocalDatabase(context)
}
