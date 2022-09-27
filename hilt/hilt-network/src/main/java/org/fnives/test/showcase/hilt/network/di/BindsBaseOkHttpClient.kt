package org.fnives.test.showcase.hilt.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
abstract class BindsBaseOkHttpClient {

    @Binds
    @SessionLessQualifier
    abstract fun bindsSessionLess(okHttpClient: OkHttpClient): OkHttpClient
}
