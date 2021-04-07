package org.fnives.test.showcase.network.mockserver

import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import java.net.InetAddress

class MockServerScenarioSetup internal constructor(
    private val networkDispatcher: NetworkDispatcher,
    private val scenarioToRequestScenario: ScenarioToRequestScenario
) {

    constructor() : this(NetworkDispatcher(), ScenarioToRequestScenario())

    lateinit var mockWebServer: MockWebServer
        private set
    var clientCertificates: HandshakeCertificates? = null
        private set

    fun start(useHttps: Boolean) {
        val mockWebServer = MockWebServer().also { this.mockWebServer = it }
        if (useHttps) {
            clientCertificates = mockWebServer.useHttps()
        }
        mockWebServer.dispatcher = networkDispatcher
        mockWebServer.start(InetAddress.getLocalHost(), PORT)
    }

    /**
     * Sets AuthScenario to what to return to the Refresh token request
     * @param validateArguments if true the request type / body / headers will be verified, otherwise just the path
     */
    fun setScenario(authScenario: AuthScenario, validateArguments: Boolean = true) = apply {
        networkDispatcher.set(
            NetworkDispatcher.ScenarioType.AUTH,
            scenarioToRequestScenario.get(authScenario, validateArguments)
        )
    }

    /**
     * Sets Scenario to what to return to the Refresh token request
     * @param validateArguments if true the request type / body / headers will be verified, otherwise just the path
     */
    fun setScenario(refreshTokenScenario: RefreshTokenScenario, validateArguments: Boolean = true) = apply {
        networkDispatcher.set(
            NetworkDispatcher.ScenarioType.REFRESH,
            scenarioToRequestScenario.get(refreshTokenScenario, validateArguments)
        )
    }

    /**
     * Sets ContentScenario to what to return to the Refresh token request
     * @param validateArguments if true the request type / body / headers will be verified, otherwise just the path
     */
    fun setScenario(contentScenario: ContentScenario, validateArguments: Boolean = true) = apply {
        networkDispatcher.set(
            NetworkDispatcher.ScenarioType.CONTENT,
            scenarioToRequestScenario.get(contentScenario, validateArguments)
        )
    }

    fun takeRequest() = mockWebServer.takeRequest()

    fun stop() {
        mockWebServer.shutdown()
    }

    companion object {
        const val PORT: Int = 7335
        val HTTP_BASE_URL get() = "http://${InetAddress.getLocalHost().hostName}"
        val HTTPS_BASE_URL get() = "https://localhost"

        private fun MockWebServer.useHttps(): HandshakeCertificates {
            val localhost = InetAddress.getByName("localhost").canonicalHostName
            val localhostCertificate = HeldCertificate.Builder()
                .addSubjectAlternativeName(localhost)
                .build()

            val serverCertificates = HandshakeCertificates.Builder()
                .heldCertificate(localhostCertificate)
                .build()

            useHttps(serverCertificates.sslSocketFactory(), false)

            val clientCertificates = HandshakeCertificates.Builder()
                .addTrustedCertificate(localhostCertificate.certificate)
                .build()

            return clientCertificates
        }
    }
}
