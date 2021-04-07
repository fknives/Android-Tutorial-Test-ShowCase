package org.fnives.test.showcase.network.mockserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.fnives.test.showcase.network.mockserver.scenario.RequestScenario
import org.fnives.test.showcase.network.mockserver.scenario.general.NotFoundRequestScenario

internal class NetworkDispatcher : Dispatcher() {

    private var scenarios: Map<ScenarioType, RequestScenario> = emptyMap()

    override fun dispatch(request: RecordedRequest): MockResponse =
        scenarios.values
            .asSequence()
            .mapNotNull { it.getResponse(request) }
            .firstOrNull()
            ?: NotFoundRequestScenario.getResponse(request)

    fun set(type: ScenarioType, scenario: RequestScenario) {
        scenarios = scenarios.plus(type to scenario)
    }

    enum class ScenarioType {
        AUTH,
        REFRESH,
        CONTENT
    }
}
