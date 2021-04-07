package org.fnives.test.showcase.network.mockserver.scenario

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

internal class RequestScenarioChain(
    private val current: RequestScenario,
    private val next: RequestScenario
) : RequestScenario {

    private var isConsumed = false

    override fun getResponse(request: RecordedRequest): MockResponse? =
        if (isConsumed) {
            next.getResponse(request)
        } else {
            current.getResponse(request)?.also { isConsumed = true }
        }
}
