package org.fnives.test.showcase.network.mockserver.scenario.general

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.fnives.test.showcase.network.mockserver.scenario.RequestScenario

internal object NotFoundRequestScenario : RequestScenario {
    override fun getResponse(request: RecordedRequest): MockResponse =
        MockResponse().setResponseCode(404).setBody("Not Found")
}
