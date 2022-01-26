package org.fnives.test.showcase.testutils

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.core.context.stopKoin

class ReloadKoinModulesIfNecessaryTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                // TODO
//                if (GlobalContext.getOrNull() == null) {
//                    val application =
//                        ApplicationProvider.getApplicationContext<TestShowcaseApplication>()
//                    startKoin {
//                        androidContext(application)
//                        modules(createAppModules(BaseUrlProvider.get()))
//                    }
//                }
                try {
                    base.evaluate()
                } finally {
                    stopKoin()
                }
            }
        }
}
