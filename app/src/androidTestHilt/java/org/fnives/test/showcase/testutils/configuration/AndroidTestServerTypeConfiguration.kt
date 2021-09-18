package org.fnives.test.showcase.testutils.configuration

import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup

object AndroidTestServerTypeConfiguration : ServerTypeConfiguration {
    override val useHttps: Boolean get() = true

    override val url: String get() = "${MockServerScenarioSetup.HTTPS_BASE_URL}:${MockServerScenarioSetup.PORT}/"

    override fun invoke(mockServerScenarioSetup: MockServerScenarioSetup) {
        val handshakeCertificates = mockServerScenarioSetup.clientCertificates ?: return
        HttpsConfigurationModule.handshakeCertificates = handshakeCertificates
    }
}
