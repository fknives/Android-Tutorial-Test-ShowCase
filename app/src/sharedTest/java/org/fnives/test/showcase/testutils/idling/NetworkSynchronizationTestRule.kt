package org.fnives.test.showcase.testutils.idling

import androidx.annotation.CheckResult
import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.core.qualifier.StringQualifier
import org.koin.test.KoinTest
import org.koin.test.get

class NetworkSynchronizationTestRule : TestRule, KoinTest {

    private var disposable: Disposable? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                disposable = registerNetworkingSynchronization()
                try {
                    base.evaluate()
                } finally {
                    dispose()
                }
            }
        }
    }

    fun dispose() = disposable?.dispose()

    @CheckResult
    private fun registerNetworkingSynchronization(): Disposable {
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
