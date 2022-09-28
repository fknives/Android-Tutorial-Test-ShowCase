package org.fnives.test.showcase.hilt.test.shared.testutils

import org.fnives.test.showcase.hilt.network.testutil.HttpsConfigurationModuleTemplate
import org.fnives.test.showcase.hilt.test.shared.di.TestBaseUrlHolder
import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MockServerScenarioSetupTestRule : TestRule {

    lateinit var mockServerScenarioSetup: MockServerScenarioSetup

    override fun apply(base: Statement, description: Description): Statement = createStatement(base)

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
        val (mockServerScenarioSetup, url) = HttpsConfigurationModuleTemplate.startWithHTTPSMockWebServer()
        TestBaseUrlHolder.url = url
        this.mockServerScenarioSetup = mockServerScenarioSetup
    }

    private fun after() {
        mockServerScenarioSetup.stop()
    }
}
