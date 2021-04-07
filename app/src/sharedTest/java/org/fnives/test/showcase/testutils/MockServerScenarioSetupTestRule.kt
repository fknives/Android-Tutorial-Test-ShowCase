package org.fnives.test.showcase.testutils

import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.testutils.configuration.ServerTypeConfiguration
import org.fnives.test.showcase.testutils.configuration.SpecificTestConfigurationsFactory
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MockServerScenarioSetupTestRule(
    val serverTypeConfiguration: ServerTypeConfiguration = SpecificTestConfigurationsFactory.createServerTypeConfiguration()
) : TestRule {
    lateinit var mockServerScenarioSetup: MockServerScenarioSetup

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
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
        mockServerScenarioSetup.start(serverTypeConfiguration.useHttps)
    }

    private fun after() {
        mockServerScenarioSetup.stop()
    }
}
