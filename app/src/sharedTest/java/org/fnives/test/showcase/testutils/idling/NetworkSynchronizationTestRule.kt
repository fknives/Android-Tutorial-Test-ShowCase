package org.fnives.test.showcase.testutils.idling

import androidx.annotation.CheckResult
import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient
import org.fnives.test.showcase.network.testutil.NetworkTestConfigurationHelper
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.test.KoinTest

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
        val idlingResources = NetworkTestConfigurationHelper.getOkHttpClients()//.filterIndexed { index, okHttpClient -> index == 0 }
            .associateBy(keySelector = { it.toString() })
            .map { (key, client) -> client.asIdlingResource(key) }
            .map(::IdlingResourceDisposable)

        return CompositeDisposable(idlingResources)
    }

    private fun OkHttpClient.asIdlingResource(name: String): IdlingResource =
        OkHttp3IdlingResource.create(name, this)
}
