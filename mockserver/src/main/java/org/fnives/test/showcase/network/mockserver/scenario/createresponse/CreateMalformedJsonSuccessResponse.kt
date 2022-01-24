package org.fnives.test.showcase.network.mockserver.scenario.createresponse

import okhttp3.mockwebserver.MockResponse

internal class CreateMalformedJsonSuccessResponse : CreateResponse {
    override fun getResponse(): MockResponse = MockResponse().setResponseCode(200).setBody("{")
}
