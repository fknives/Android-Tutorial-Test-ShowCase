package org.fnives.test.showcase.testutils

import androidx.test.core.app.ApplicationProvider
import org.fnives.test.showcase.BuildConfig
import org.fnives.test.showcase.TestShowcaseApplication
import org.fnives.test.showcase.di.createAppModules
import org.fnives.test.showcase.model.network.BaseUrl
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTest

/**
 * Test rule to help reinitialize the whole Koin setup.
 *
 * It's needed because in AndroidTest's the Application is only called once,
 * meaning our koin would be shared.
 *
 * Note: Do not use if you want your test's to share Koin, and in such case do not stop your Koin.
 */
open class ReloadKoinModulesIfNecessaryTestRule : TestRule, KoinTest {
    override fun apply(base: Statement, description: Description): Statement =
        ReinitKoinStatement(base)

    class ReinitKoinStatement(private val base: Statement) : Statement() {
        override fun evaluate() {
            reinitKoinIfNeeded()
            try {
                base.evaluate()
            } finally {
                stopKoin()
            }
        }

        private fun reinitKoinIfNeeded() {
            if (KoinPlatformTools.defaultContext().getOrNull() != null) return

            val application = ApplicationProvider.getApplicationContext<TestShowcaseApplication>()
            val baseUrl = BaseUrl(BuildConfig.BASE_URL)
            startKoin {
                androidContext(application)
                modules(createAppModules(baseUrl))
            }
        }
    }
}
