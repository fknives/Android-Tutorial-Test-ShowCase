package org.fnives.test.showcase.testutils

import androidx.test.core.app.ApplicationProvider
import org.fnives.test.showcase.TestShowcaseApplication
import org.fnives.test.showcase.di.BaseUrlProvider
import org.fnives.test.showcase.di.createAppModules
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class ReloadKoinModulesIfNecessaryTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                if (GlobalContext.getOrNull() == null) {
                    val application = ApplicationProvider.getApplicationContext<TestShowcaseApplication>()
                    startKoin {
                        androidContext(application)
                        modules(createAppModules(BaseUrlProvider.get()))
                    }
                }
                try {
                    base.evaluate()
                } finally {
                    stopKoin()
                }
            }
        }
}
