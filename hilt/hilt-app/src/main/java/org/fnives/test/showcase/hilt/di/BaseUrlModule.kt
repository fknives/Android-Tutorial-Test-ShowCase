package org.fnives.test.showcase.hilt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fnives.test.showcase.hilt.BuildConfig

@InstallIn(SingletonComponent::class)
@Module

object BaseUrlModule {

    @Provides
    fun provideBaseUrl(): String = BuildConfig.BASE_URL
}
