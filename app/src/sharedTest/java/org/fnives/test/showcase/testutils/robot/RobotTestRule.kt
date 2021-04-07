package org.fnives.test.showcase.testutils.robot

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RobotTestRule<T : Robot>(val robot: T) : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                robot.init()
                try {
                    base.evaluate()
                } finally {
                    robot.release()
                }
            }
        }
}
