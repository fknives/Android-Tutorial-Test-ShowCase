package org.fnives.test.showcase.hilt.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.fnives.test.showcase.hilt.network.auth.LoginRemoteSource
import org.fnives.test.showcase.hilt.network.auth.LoginRemoteSourceImpl
import org.fnives.test.showcase.hilt.network.auth.LoginService
import org.fnives.test.showcase.hilt.network.content.ContentRemoteSource
import org.fnives.test.showcase.hilt.network.content.ContentRemoteSourceImpl
import org.fnives.test.showcase.hilt.network.content.ContentService
import org.fnives.test.showcase.hilt.network.session.AuthenticationHeaderInterceptor
import org.fnives.test.showcase.hilt.network.session.SessionAuthenticator
import org.fnives.test.showcase.hilt.network.shared.PlatformInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object HiltNetworkModule {

    @Provides
    @Singleton
    fun provideConverterFactory(): Converter.Factory = MoshiConverterFactory.create()

    @Provides
    @Singleton
    fun provideSessionLessOkHttpClient(enableLogging: Boolean, platformInterceptor: PlatformInterceptor) =
        OkHttpClient.Builder()
            .addInterceptor(platformInterceptor)
            .setupLogging(enableLogging)
            .build()

    @Provides
    @Singleton
    @SessionLessQualifier
    fun provideSessionLessRetrofit(
        baseUrl: String,
        converterFactory: Converter.Factory,
        @SessionLessQualifier okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(converterFactory)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    @SessionQualifier
    internal fun provideSessionOkHttpClient(
        @SessionLessQualifier okHttpClient: OkHttpClient,
        sessionAuthenticator: SessionAuthenticator,
        authenticationHeaderInterceptor: AuthenticationHeaderInterceptor,
    ) =
        okHttpClient
            .newBuilder()
            .authenticator(sessionAuthenticator)
            .addInterceptor(authenticationHeaderInterceptor)
            .build()

    @Provides
    @Singleton
    @SessionQualifier
    fun provideSessionRetrofit(@SessionLessQualifier retrofit: Retrofit, @SessionQualifier okHttpClient: OkHttpClient): Retrofit =
        retrofit.newBuilder()
            .client(okHttpClient)
            .build()

    @Provides
    internal fun bindContentRemoteSource(contentRemoteSourceImpl: ContentRemoteSourceImpl): ContentRemoteSource = contentRemoteSourceImpl

    @Provides
    internal fun bindLoginRemoteSource(loginRemoteSource: LoginRemoteSourceImpl): LoginRemoteSource = loginRemoteSource

    @Provides
    internal fun provideLoginService(@SessionLessQualifier retrofit: Retrofit): LoginService =
        retrofit.create(LoginService::class.java)

    @Provides
    internal fun provideContentService(@SessionQualifier retrofit: Retrofit): ContentService =
        retrofit.create(ContentService::class.java)
}
