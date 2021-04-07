package org.fnives.test.showcase.network.mockserver.scenario.createresponse

import okhttp3.mockwebserver.MockResponse

internal class CreateUnauthorizedResponse : CreateResponse {
    override fun getResponse(): MockResponse =
        MockResponse().setResponseCode(401).setBody("{}")
}
