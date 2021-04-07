package org.fnives.test.showcase.testutils.configuration

import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup

object RobolectricServerTypeConfiguration : ServerTypeConfiguration {
    override val useHttps: Boolean = false

    override val url: String get() = "${MockServerScenarioSetup.HTTP_BASE_URL}:${MockServerScenarioSetup.PORT}/"

    override fun invoke(mockServerScenarioSetup: MockServerScenarioSetup) = Unit
}
