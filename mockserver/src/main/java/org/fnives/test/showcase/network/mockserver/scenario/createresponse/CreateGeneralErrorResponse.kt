package org.fnives.test.showcase.network.mockserver.scenario.createresponse

import okhttp3.mockwebserver.MockResponse

internal class CreateGeneralErrorResponse : CreateResponse {
    override fun getResponse(): MockResponse = MockResponse().setResponseCode(500).setBody("{}")
}
