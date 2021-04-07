package org.fnives.test.showcase.network.mockserver.scenario

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateResponse
import org.json.JSONException

internal class SpecificRequestScenario(
    private val requestMatchingChecker: RequestMatchingChecker,
    private val createResponse: CreateResponse
) : RequestScenario {

    override fun getResponse(request: RecordedRequest): MockResponse? = wrapExceptionsIntoMockResponse {
        if (requestMatchingChecker.isValidRequest(request)) {
            createResponse.getResponse()
        } else {
            null
        }
    }

    private fun wrapExceptionsIntoMockResponse(responseFactory: () -> MockResponse?): MockResponse? =
        try {
            responseFactory()
        } catch (jsonException: JSONException) {
            MockResponse().setBody("JSONException while asserting your request, message: ${jsonException.message}")
                .setResponseCode(400)
        } catch (assertionError: AssertionError) {
            MockResponse().setBody("AssertionError while asserting your request, message: ${assertionError.message}")
                .setResponseCode(400)
        } catch (throwable: Throwable) {
            MockResponse().setBody("Unexpected Exception while asserting your request, message: ${throwable.message}")
                .setResponseCode(400)
        }
}
