package org.fnives.test.showcase.network.mockserver.scenario.createresponse

import okhttp3.mockwebserver.MockResponse

internal interface CreateResponse {

    fun getResponse(): MockResponse
}
