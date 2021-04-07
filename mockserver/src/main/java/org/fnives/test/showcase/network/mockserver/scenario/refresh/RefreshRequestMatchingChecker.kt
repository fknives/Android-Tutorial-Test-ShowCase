package org.fnives.test.showcase.network.mockserver.scenario.refresh

import okhttp3.mockwebserver.RecordedRequest
import org.fnives.test.showcase.network.mockserver.scenario.RequestMatchingChecker
import org.junit.Assert

internal class RefreshRequestMatchingChecker(val validateArguments: Boolean) : RequestMatchingChecker {
    override fun isValidRequest(request: RecordedRequest): Boolean {
        if (request.path != "/login/login-refresh" && request.path != "/login/refreshed-refresh") {
            return false
        }
        if (!validateArguments) return true

        Assert.assertEquals("PUT", request.method)
        Assert.assertEquals("Android", request.getHeader("Platform"))
        Assert.assertEquals(null, request.getHeader("Authorization"))
        Assert.assertEquals("", request.body.readUtf8())

        return true
    }
}
