package org.fnives.test.showcase.network.mockserver.scenario.refresh

import okhttp3.mockwebserver.MockResponse
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateResponse
import org.fnives.test.showcase.network.mockserver.utils.readResourceFile

internal class CreateRefreshResponse : CreateResponse {
    override fun getResponse(): MockResponse {
        val responseBody = readResourceFile("response/refresh/success_response_refresh.json")
        return MockResponse().setResponseCode(200).setBody(responseBody)
    }
}
