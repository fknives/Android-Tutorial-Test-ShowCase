package org.fnives.test.showcase.network.di.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.fnives.test.showcase.hilt.SessionLessQualifier

@InstallIn(SingletonComponent::class)
@Module
abstract class BindsBaseOkHttpClient {

    @Binds
    @SessionLessQualifier
    abstract fun bindsSessionLess(okHttpClient: OkHttpClient) : OkHttpClient
}