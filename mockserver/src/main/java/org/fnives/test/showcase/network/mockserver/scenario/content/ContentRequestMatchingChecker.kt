package org.fnives.test.showcase.network.mockserver.scenario.content

import okhttp3.mockwebserver.RecordedRequest
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.RequestMatchingChecker
import org.junit.Assert

internal class ContentRequestMatchingChecker(
    private val contentScenario: ContentScenario,
    private val validateArguments: Boolean
) : RequestMatchingChecker {

    override fun isValidRequest(request: RecordedRequest): Boolean {
        if (request.path != "/content") return false
        if (!validateArguments) return true

        Assert.assertEquals("GET", request.method)
        Assert.assertEquals("Android", request.getHeader("Platform"))
        val expectedToken = if (contentScenario.usingRefreshedToken) {
            ContentData.refreshSuccessResponse.accessToken
        } else {
            ContentData.loginSuccessResponse.accessToken
        }
        Assert.assertEquals(expectedToken, request.getHeader("Authorization"))
        Assert.assertEquals("", request.body.readUtf8())

        return true
    }
}
