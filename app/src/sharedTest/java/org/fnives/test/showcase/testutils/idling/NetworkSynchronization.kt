package org.fnives.test.showcase.testutils.idling

import androidx.annotation.CheckResult
import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient
import org.koin.core.qualifier.StringQualifier
import org.koin.test.KoinTest
import org.koin.test.get

object NetworkSynchronization : KoinTest {

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

    private fun getOkHttpClient(type: OkHttpClientTypes): OkHttpClient = get(type.asQualifier())

    private fun OkHttpClient.asIdlingResource(name: String): IdlingResource =
        OkHttp3IdlingResource.create(name, this)

    enum class OkHttpClientTypes(val qualifier: String) {
        SESSION("SESSION-NETWORKING"), SESSIONLESS("SESSIONLESS-NETWORKING");

        fun asQualifier() = StringQualifier(qualifier)
    }
}
