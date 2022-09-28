package org.fnives.test.showcase.ui.compose.idle

import android.util.Log
import androidx.annotation.CheckResult
import androidx.compose.ui.test.junit4.ComposeTestRule
import okhttp3.OkHttpClient
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.CompositeDisposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.Disposable
import org.fnives.test.showcase.android.testutil.synchronization.idlingresources.OkHttp3IdlingResource
import org.fnives.test.showcase.network.testutil.NetworkTestConfigurationHelper
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.test.KoinTest

class ComposeNetworkSynchronizationTestRule(private val composeTestRule: ComposeTestRule) : TestRule, KoinTest {

    private var disposable: Disposable? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                disposable = registerIdlingResources()
                try {
                    base.evaluate()
                } finally {
                    dispose()
                }
            }
        }
    }

    fun dispose() {
        if (disposable == null) {
            Log.w("ComposeNetworkSynchronizationTestRule", "Was disposed, but registerIdlingResources was not called!")
        }
        disposable?.dispose()
    }

    @CheckResult
    private fun registerIdlingResources(): Disposable = getOkHttpClients()
        .associateBy(keySelector = { it.toString() })
        .map { (key, client) -> OkHttp3IdlingResource.create(key, client) }
        .map(::EspressoToComposeIdlingResourceAdapter)
        .map { ComposeIdlingDisposable(it, composeTestRule) }
        .let(::CompositeDisposable)

    private fun getOkHttpClients(): List<OkHttpClient> =
        NetworkTestConfigurationHelper.getOkHttpClients()
}
