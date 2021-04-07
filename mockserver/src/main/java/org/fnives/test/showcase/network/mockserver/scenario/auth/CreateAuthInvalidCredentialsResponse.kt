package org.fnives.test.showcase.network.mockserver.scenario.auth

import okhttp3.mockwebserver.MockResponse
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateResponse

internal class CreateAuthInvalidCredentialsResponse : CreateResponse {

    override fun getResponse(): MockResponse =
        MockResponse().setResponseCode(400).setBody("{}")
}
