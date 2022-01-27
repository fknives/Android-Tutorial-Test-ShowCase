package org.fnives.test.showcase.network.di

import okhttp3.OkHttpClient
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.auth.LoginErrorConverter
import org.fnives.test.showcase.network.auth.LoginRemoteSource
import org.fnives.test.showcase.network.auth.LoginRemoteSourceImpl
import org.fnives.test.showcase.network.auth.LoginService
import org.fnives.test.showcase.network.content.ContentRemoteSource
import org.fnives.test.showcase.network.content.ContentRemoteSourceImpl
import org.fnives.test.showcase.network.content.ContentService
import org.fnives.test.showcase.network.session.AuthenticationHeaderInterceptor
import org.fnives.test.showcase.network.session.AuthenticationHeaderUtils
import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.session.SessionAuthenticator
import org.fnives.test.showcase.network.shared.PlatformInterceptor
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun createNetworkModules(
    baseUrl: BaseUrl,
    enableLogging: Boolean,
    networkSessionLocalStorageProvider: Scope.() -> NetworkSessionLocalStorage,
    networkSessionExpirationListenerProvider: Scope.() -> NetworkSessionExpirationListener
): Sequence<Module> =
    sequenceOf(
        baseUrlModule(baseUrl),
        loginModule(),
        contentModule(),
        sessionlessNetworkingModule(enableLogging),
        sessionNetworkingModule(networkSessionLocalStorageProvider, networkSessionExpirationListenerProvider)
    )

private fun baseUrlModule(baseUrl: BaseUrl) = module {
    single { baseUrl }
}

private fun loginModule() = module {
    factory { LoginRemoteSourceImpl(get(), get()) }
    factory<LoginRemoteSource> { get<LoginRemoteSourceImpl>() }
    factory { LoginErrorConverter() }
    factory { get<Retrofit>(sessionless).create(LoginService::class.java) }
}

private fun contentModule() = module {
    factory { get<Retrofit>(session).create(ContentService::class.java) }
    factory { ContentRemoteSourceImpl(get()) }
    factory<ContentRemoteSource> { get<ContentRemoteSourceImpl>() }
}

private fun sessionlessNetworkingModule(enableLogging: Boolean) = module {
    factory { MoshiConverterFactory.create() }
    single(qualifier = sessionless) {
        OkHttpClient.Builder()
            .addInterceptor(PlatformInterceptor())
            .setupLogging(enableLogging)
            .build()
    }
    single(qualifier = sessionless) {
        Retrofit.Builder()
            .baseUrl(get<BaseUrl>().baseUrl)
            .addConverterFactory(get<MoshiConverterFactory>())
            .client(get(sessionless))
            .build()
    }
}

private fun sessionNetworkingModule(
    networkSessionLocalStorageProvider: Scope.() -> NetworkSessionLocalStorage,
    networkSessionExpirationListenerProvider: Scope.() -> NetworkSessionExpirationListener
) = module {
    single { AuthenticationHeaderUtils(get()) }
    single { networkSessionExpirationListenerProvider() }
    single { networkSessionLocalStorageProvider() }
    factory { SessionAuthenticator(get(), get(), get(), get()) }
    single(qualifier = session) {
        get<OkHttpClient>(sessionless)
            .newBuilder()
            .authenticator(get<SessionAuthenticator>())
            .addInterceptor(AuthenticationHeaderInterceptor(get()))
            .build()
    }
    single(qualifier = session) { get<Retrofit>(sessionless).newBuilder().client(get(session)).build() }
}

internal val session = StringQualifier("SESSION-NETWORKING")
internal val sessionless = StringQualifier("SESSIONLESS-NETWORKING")
