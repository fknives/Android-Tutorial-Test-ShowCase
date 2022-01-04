package org.fnives.test.showcase.testutils.idling

import androidx.annotation.CheckResult
import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient
import org.fnives.test.showcase.hilt.SessionLessQualifier
import org.fnives.test.showcase.hilt.SessionQualifier
import javax.inject.Inject

class NetworkSynchronization @Inject constructor(
    @SessionQualifier
    private val sessionOkhttpClient: OkHttpClient,
    @SessionLessQualifier
    private val sessionlessOkhttpClient: OkHttpClient
) {

    @CheckResult
    fun registerNetworkingSynchronization(): Disposable {
        val idlingResources = OkHttpClientTypes.values()
            .map { it to getOkHttpClient(it) }
            .associateBy { it.second.dispatcher }
            .values
            .map { (key, client) -> client.asIdlingResource(key.qualifier) }
            .map(::IdlingResourceDisposable)

        return CompositeDisposable(idlingResources)
    }

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
