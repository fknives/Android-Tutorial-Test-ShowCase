package org.fnives.test.showcase.hilt.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.storage.LocalDatabase
import org.fnives.test.showcase.hilt.storage.database.DatabaseInitialization
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object StorageModule {
    @Singleton
    @Provides
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase =
        DatabaseInitialization.create(context)
}
