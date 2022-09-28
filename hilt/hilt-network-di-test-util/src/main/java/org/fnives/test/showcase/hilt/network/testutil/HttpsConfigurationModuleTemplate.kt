package org.fnives.test.showcase.hilt.network.testutil

import okhttp3.tls.HandshakeCertificates
import org.fnives.test.showcase.hilt.network.di.HiltNetworkModule
import org.fnives.test.showcase.hilt.network.shared.PlatformInterceptor
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup

//  @Module
//  @TestInstallIn(
//      components = [SingletonComponent::class],
//      replaces = [BindsBaseOkHttpClient::class]
//  )
object HttpsConfigurationModuleTemplate {

    lateinit var handshakeCertificates: HandshakeCertificates

//    @Provides
//    @Singleton
//    @SessionLessQualifier
    fun bindsBaseOkHttpClient(enableLogging: Boolean, platformInterceptor: PlatformInterceptor) =
        HiltNetworkModule.provideSessionLessOkHttpClient(enableLogging, platformInterceptor)
            .newBuilder()
            .sslSocketFactory(
                handshakeCertificates.sslSocketFactory(),
                handshakeCertificates.trustManager
            )
            .build()

    fun startWithHTTPSMockWebServer(): Pair<MockServerScenarioSetup, String> {
        val mockServerScenarioSetup = MockServerScenarioSetup()
        val url = mockServerScenarioSetup.start(true)

        handshakeCertificates = mockServerScenarioSetup.clientCertificates
            ?: throw IllegalStateException("ClientCertificate should be accessable")

        return mockServerScenarioSetup to url
    }
}
