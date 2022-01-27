package org.fnives.test.showcase.testutils.configuration

import androidx.annotation.StringRes
import org.junit.rules.TestRule

interface SnackbarVerificationHelper : TestRule {

    fun assertIsShownWithText(@StringRes stringResID: Int)

    fun assertIsNotShown()
}

@Suppress("TestFunctionName")
fun SnackbarVerificationTestRule(): SnackbarVerificationHelper =
    SpecificTestConfigurationsFactory.createSnackbarVerification()
