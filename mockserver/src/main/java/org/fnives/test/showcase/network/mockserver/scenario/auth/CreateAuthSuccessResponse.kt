package org.fnives.test.showcase.network.mockserver.scenario.auth

import okhttp3.mockwebserver.MockResponse
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateResponse
import org.fnives.test.showcase.network.mockserver.utils.readResourceFile

internal class CreateAuthSuccessResponse : CreateResponse {

    override fun getResponse(): MockResponse {
        val responseBody = readResourceFile("response/auth/success_response_login.json")
        return MockResponse().setResponseCode(200).setBody(responseBody)
    }
}
