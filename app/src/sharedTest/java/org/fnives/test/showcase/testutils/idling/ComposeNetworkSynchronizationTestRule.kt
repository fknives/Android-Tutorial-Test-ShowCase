package org.fnives.test.showcase.testutils.idling

import androidx.annotation.CheckResult
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.junit4.ComposeTestRule
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
        val idlingResources = NetworkTestConfigurationHelper.getOkHttpClients()
            .associateBy(keySelector = { it.toString() })
            .map { (key, client) -> OkHttp3IdlingResource.create(key, client) }
            .map {
                ComposeIdlingResourceDisposable(composeTestRule, object : IdlingResource {
                    override val isIdleNow: Boolean
                        get() {
                            return it.isIdleNow
                        }
                })
            }

        return CompositeDisposable(idlingResources)
    }
}


private class ComposeIdlingResourceDisposable(
    private val composeTestRule: ComposeTestRule,
    private val idlingResource: IdlingResource
) : Disposable {
    override var isDisposed: Boolean = false
        private set

    init {
        composeTestRule.registerIdlingResource(idlingResource)
    }

    override fun dispose() {
        if (isDisposed) return
        isDisposed = true
        composeTestRule.unregisterIdlingResource(idlingResource)
    }
}