package org.fnives.test.showcase.testutils.configuration

import androidx.annotation.StringRes
import org.junit.rules.TestRule

interface SnackbarVerificationTestRule : TestRule {

    fun assertIsShownWithText(@StringRes stringResID: Int)

    fun assertIsNotShown()
}
