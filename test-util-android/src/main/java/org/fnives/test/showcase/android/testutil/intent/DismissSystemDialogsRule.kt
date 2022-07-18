package org.fnives.test.showcase.android.testutil.intent

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Test Rule to workaround ANRs from other applications.
 *
 * ANRs on Emulators can cause or Test Activity to not receive focus and fail our Tests.
 * To workaround this, this TestRule, checks the Espresso exception, if found dismisses the ANR and reruns our test.
 *
 * This Test Rule should be applied before every other setup, because when retrying the setup should still be clean.
 */
class DismissSystemDialogsRule(
    private val anrLimit: Int = 3,
    private val anrPossibleWaitMessages: Set<String> = defaultANRPossibleWaitMessages,
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            var anrCount = 0
            var testFinished = false
            while (!testFinished) {
                try {
                    log("Run test method = ${description.testName}, anrCount = $anrCount")
                    base.evaluate()
                    testFinished = true
                    log("Test success = ${description.testName}, anrCount = $anrCount")
                } catch (throwable: Throwable) {
                    if (throwable.isANRDialog() && anrCount < anrLimit) {
                        log("ANR found = ${description.testName}, anrCount = $anrCount")
                        anrCount++
                        handleAnrDialogue()
                    } else {
                        log("Exception found = ${description.testName}, anrCount = $anrCount")
                        throw throwable
                    }
                }
            }
        }
    }

    private fun Throwable.isANRDialog() =
        message?.contains(ANR_DIALOG_ESPRESSO_EXCEPTION_MESSAGE) == true

    private fun handleAnrDialogue() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        anrPossibleWaitMessages.first {
            val waitButton = device.findObject(UiSelector().textContains(it))
            val exists = waitButton.exists()
            if (exists) {
                waitButton.click()
            }
            exists
        }
    }

    fun log(message: String) {
        Log.d(TAG, message)
    }

    companion object {
        const val TAG = "DismissSysDialog"
        private val Description.testName get() = "${testClass.simpleName}:$methodName"

        private val defaultANRPossibleWaitMessages = setOf(
            "wait", // en
            "待機", // jp
            "ok" // en
        )
        private const val ANR_DIALOG_ESPRESSO_EXCEPTION_MESSAGE = "Waited for the root of the view hierarchy " +
            "to have window focus and not request layout for 10 seconds. If you specified a non default root matcher,"
    }
}
