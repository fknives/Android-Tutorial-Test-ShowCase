package org.fnives.test.showcase.ui.login.codekata.rule.intent

import androidx.test.espresso.intent.Intents
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Takes care of [Intents] initialization.
 */
class PlainIntentInitRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    Intents.init()
                    base.evaluate()
                } finally {
                    Intents.release()
                }
            }
        }
    }
}
