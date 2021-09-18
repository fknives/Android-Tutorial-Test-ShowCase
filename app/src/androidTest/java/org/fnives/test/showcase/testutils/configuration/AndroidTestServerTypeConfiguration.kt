package org.fnives.test.showcase.testutils.configuration

import okhttp3.OkHttpClient
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

object AndroidTestServerTypeConfiguration : ServerTypeConfiguration, KoinTest {
    override val useHttps: Boolean get() = true

    override val url: String get() = "${MockServerScenarioSetup.HTTPS_BASE_URL}:${MockServerScenarioSetup.PORT}/"

    override fun invoke(mockServerScenarioSetup: MockServerScenarioSetup) {
        val handshakeCertificates = mockServerScenarioSetup.clientCertificates ?: return
        val sessionless = StringQualifier(NetworkSynchronization.OkHttpClientTypes.SESSIONLESS.qualifier)
        val okHttpClientWithCertificate = get<OkHttpClient>(sessionless).newBuilder()
            .sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            .build()
        loadKoinModules(
            module {
                single(qualifier = sessionless) { okHttpClientWithCertificate }
            }
        )
    }
}
