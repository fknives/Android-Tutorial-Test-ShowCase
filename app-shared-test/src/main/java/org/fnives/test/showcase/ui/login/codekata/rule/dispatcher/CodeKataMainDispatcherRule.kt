package org.fnives.test.showcase.ui.login.codekata.rule.dispatcher

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@OptIn(ExperimentalCoroutinesApi::class)
class CodeKataMainDispatcherRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                TODO("Not yet implemented")
            }
        }
}
