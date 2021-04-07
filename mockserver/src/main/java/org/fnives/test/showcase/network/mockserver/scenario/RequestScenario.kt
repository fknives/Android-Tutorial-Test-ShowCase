package org.fnives.test.showcase.network.mockserver.scenario

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

internal interface RequestScenario {

    fun getResponse(request: RecordedRequest): MockResponse?
}
