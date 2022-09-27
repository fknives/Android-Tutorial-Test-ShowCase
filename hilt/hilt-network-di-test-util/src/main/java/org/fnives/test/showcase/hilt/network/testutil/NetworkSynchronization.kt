package org.fnives.test.showcase.hilt.network.testutil

import androidx.annotation.CheckResult
import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient
import org.fnives.test.showcase.hilt.network.di.SessionLessQualifier
import org.fnives.test.showcase.hilt.network.di.SessionQualifier
import javax.inject.Inject

class NetworkSynchronization @Inject constructor(
    @SessionQualifier
    private val sessionOkhttpClient: OkHttpClient,
    @SessionLessQualifier
    private val sessionlessOkhttpClient: OkHttpClient
) {

    @CheckResult
    fun networkIdlingResources(): List<IdlingResource> =
        OkHttpClientTypes.values()
            .map { it to getOkHttpClient(it) }
            .associateBy { it.second.dispatcher }
            .values
            .map { (key, client) -> client.asIdlingResource(key.qualifier) }

    private fun getOkHttpClient(type: OkHttpClientTypes): OkHttpClient =
        when (type) {
            OkHttpClientTypes.SESSION -> sessionOkhttpClient
            OkHttpClientTypes.SESSIONLESS -> sessionlessOkhttpClient
        }

    private fun OkHttpClient.asIdlingResource(name: String): IdlingResource =
        OkHttp3IdlingResource.create(name, this)

    enum class OkHttpClientTypes(val qualifier: String) {
        SESSION("SESSION-NETWORKING"), SESSIONLESS("SESSIONLESS-NETWORKING")
    }
}
