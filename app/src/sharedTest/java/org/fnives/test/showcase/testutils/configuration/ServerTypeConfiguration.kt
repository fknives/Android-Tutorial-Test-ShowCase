package org.fnives.test.showcase.testutils.configuration

import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup

interface ServerTypeConfiguration {

    val useHttps: Boolean

    val url: String

    fun invoke(mockServerScenarioSetup: MockServerScenarioSetup)
}
