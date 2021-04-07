package org.fnives.test.showcase.network.mockserver.scenario.auth

import okhttp3.mockwebserver.RecordedRequest
import org.fnives.test.showcase.network.mockserver.scenario.RequestMatchingChecker
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class AuthRequestMatchingChecker(
    private val authScenario: AuthScenario,
    private val validateArguments: Boolean
) : RequestMatchingChecker {

    override fun isValidRequest(request: RecordedRequest): Boolean {
        if (request.path != "/login") return false
        if (!validateArguments) return true

        Assert.assertEquals("POST", request.method)
        Assert.assertEquals("Android", request.getHeader("Platform"))
        Assert.assertEquals(null, request.getHeader("Authorization"))
        val expectedJson = createExpectedJson(
            username = authScenario.username,
            password = authScenario.password
        )
        JSONAssert.assertEquals(expectedJson, request.body.readUtf8(), JSONCompareMode.LENIENT)

        return true
    }

    companion object {
        internal fun createExpectedJson(username: String, password: String): String =
            """
        {
            "username": "$username",
            "password": "$password"
        }
            """.trimIndent()
    }
}
