package org.fnives.test.showcase.testutils.configuration

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.tls.HandshakeCertificates
import org.fnives.test.showcase.hilt.SessionLessQualifier
import org.fnives.test.showcase.network.di.hilt.BindsBaseOkHttpClient
import org.fnives.test.showcase.network.di.hilt.HiltNetworkModule
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BindsBaseOkHttpClient::class]
)
object HttpsConfigurationModule {

    lateinit var handshakeCertificates: HandshakeCertificates

    @Provides
    @Singleton
    @SessionLessQualifier
    fun bindsBaseOkHttpClient(enableLogging: Boolean) =
        HiltNetworkModule.provideSessionLessOkHttpClient(enableLogging)
            .newBuilder()
            .sslSocketFactory(
                handshakeCertificates.sslSocketFactory(),
                handshakeCertificates.trustManager
            )
            .build()
}