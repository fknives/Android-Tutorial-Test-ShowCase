package org.fnives.test.showcase.network.mockserver.scenario

import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONException
import kotlin.jvm.Throws

internal interface RequestMatchingChecker {

    @Throws(AssertionError::class, JSONException::class)
    fun isValidRequest(request: RecordedRequest): Boolean
}
