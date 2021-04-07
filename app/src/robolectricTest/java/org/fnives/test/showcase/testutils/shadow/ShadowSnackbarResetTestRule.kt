package org.fnives.test.showcase.testutils.shadow

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ShadowSnackbarResetTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                ShadowSnackbar.reset()
                try {
                    base.evaluate()
                } finally {
                    ShadowSnackbar.reset()
                }
            }
        }
}
