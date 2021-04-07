package org.fnives.test.showcase.network.mockserver.scenario.content

import okhttp3.mockwebserver.MockResponse
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateResponse
import org.fnives.test.showcase.network.mockserver.utils.readResourceFile

internal class CreateContentSuccessWithMissingFields : CreateResponse {

    override fun getResponse(): MockResponse {
        val responseBody = readResourceFile("response/content/content_missing_field_response.json")
        return MockResponse().setResponseCode(200).setBody(responseBody)
    }
}
