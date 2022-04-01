package org.fnives.test.showcase.testutils

import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
import org.fnives.test.showcase.network.testutil.NetworkTestConfigurationHelper
import org.fnives.test.showcase.testutils.idling.NetworkSynchronizationTestRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.test.KoinTest

/**
 * TestRule which ensures Koin is reseted between each tests and setups Network mocking.
 *
 * It First resets koin if needed.
 * Then creates and starts the mockwebserver, it also injects the correct baseUrl into the OkHttp Client.
 * Then synchronizes Espresso with the OkHttp Client
 */
class MockServerScenarioSetupResetingTestRule(
    private val reloadKoinModulesIfNecessaryTestRule: ReloadKoinModulesIfNecessaryTestRule = ReloadKoinModulesIfNecessaryTestRule(),
    private val networkSynchronizationTestRule: TestRule = NetworkSynchronizationTestRule()
) : TestRule, KoinTest {

    lateinit var mockServerScenarioSetup: MockServerScenarioSetup

    override fun apply(base: Statement, description: Description): Statement =
        networkSynchronizationTestRule.apply(base, description)
            .let(::createStatement)
            .let { reloadKoinModulesIfNecessaryTestRule.apply(it, description) }

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
        mockServerScenarioSetup = NetworkTestConfigurationHelper.startWithHTTPSMockWebServer()
    }

    private fun after() {
        mockServerScenarioSetup.stop()
    }
}
