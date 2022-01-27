package org.fnives.test.showcase.network.testutil

import okhttp3.OkHttpClient
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

/**
 * Gives access to internals of Networking so it can be used in MockWebServer more easily.
 */
object NetworkTestConfigurationHelper : KoinTest {

    /**
     * For some reason importing these didn't work. Still keeping internal, cause it shouldn't leave the module.
     *
     * import org.fnives.test.showcase.network.di.session
     * import org.fnives.test.showcase.network.di.sessionless
     */
    internal val session = StringQualifier("SESSION-NETWORKING")
    internal val sessionless = StringQualifier("SESSIONLESS-NETWORKING")

    /**
     * After koin started, this gives you access for the OkHttpClients, so you can synchronize or keep track of them
     */
    fun getOkHttpClients(): List<OkHttpClient> = listOf(
        get<OkHttpClient>(sessionless),
        get<OkHttpClient>(session)
    )

    /**
     * After koin started, this sets up MockServer to be used with HTTPs.
     *
     * Url, and injected OkHttpClient is modified for this.
     */
    fun startWithHTTPSMockWebServer(): MockServerScenarioSetup{
        val mockServerScenarioSetup = MockServerScenarioSetup()
        val url = mockServerScenarioSetup.start(true)

        val handshakeCertificates = mockServerScenarioSetup.clientCertificates
            ?: throw IllegalStateException("ClientCertificate should be accessable")

        reload(baseUrl = BaseUrl(url)) {
            it.newBuilder()
                .sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
                .build()
        }

        return mockServerScenarioSetup
    }

    private fun reload(baseUrl: BaseUrl, adjustments: (OkHttpClient) -> OkHttpClient) {
        val current = get<OkHttpClient>(sessionless)

        val adjusted = adjustments(current)
        loadKoinModules(
            module {
                // add https certificate to okhttp
                single(qualifier = sessionless) { adjusted }
                // replace base url with mockWebServer's
                single { baseUrl }
            }
        )
    }
}