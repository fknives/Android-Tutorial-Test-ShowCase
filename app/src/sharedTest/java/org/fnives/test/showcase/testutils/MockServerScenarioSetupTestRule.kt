package org.fnives.test.showcase.testutils

import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.testutils.idling.NetworkSynchronization.OkHttpClientTypes
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class MockServerScenarioSetupTestRule : ReloadKoinModulesIfNecessaryTestRule(), KoinTest {

    lateinit var mockServerScenarioSetup: MockServerScenarioSetup

    private val sessionlessQualifier get() = OkHttpClientTypes.SESSIONLESS.asQualifier()

    override fun apply(base: Statement, description: Description): Statement =
        super.apply(createStatement(base), description)

    private fun createStatement(base: Statement) = object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            before()
            try {
                base.evaluate()
            } finally {
                after()
            }
        }
    }

    private fun before() {
        mockServerScenarioSetup = MockServerScenarioSetup()
        val url = mockServerScenarioSetup.start(true)

        val handshakeCertificates = mockServerScenarioSetup.clientCertificates
            ?: throw IllegalStateException("ClientCertificate should be accessable")

        val okHttpClientWithCertificate = createUpdateOkHttpClient(handshakeCertificates)

        loadKoinModules(
            module {
                // add https certificate to okhttp
                single(qualifier = sessionlessQualifier) { okHttpClientWithCertificate }
                // replace base url with mockWebServer's
                single { BaseUrl(url) }
            }
        )
    }

    private fun createUpdateOkHttpClient(handshakeCertificates: HandshakeCertificates) =
        get<OkHttpClient>(sessionlessQualifier).newBuilder()
            .sslSocketFactory(handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager)
            .build()


    private fun after() {
        mockServerScenarioSetup.stop()
    }
}
