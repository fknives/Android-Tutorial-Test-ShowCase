package org.fnives.test.showcase.network.shared

import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class MockServerScenarioSetupExtensions : BeforeEachCallback, AfterEachCallback {

    val url: String = "${MockServerScenarioSetup.HTTP_BASE_URL}:${MockServerScenarioSetup.PORT}/"
    lateinit var mockServerScenarioSetup: MockServerScenarioSetup

    override fun beforeEach(context: ExtensionContext?) {
        mockServerScenarioSetup = MockServerScenarioSetup()
        mockServerScenarioSetup.start(false)
    }

    override fun afterEach(context: ExtensionContext?) {
        mockServerScenarioSetup.stop()
    }
}
