package org.fnives.test.showcase.testutils.configuration

import androidx.annotation.StringRes
import org.fnives.test.showcase.testutils.shadow.ShadowSnackbar
import org.fnives.test.showcase.testutils.shadow.ShadowSnackbarResetTestRule
import org.junit.Assert
import org.junit.rules.TestRule

object RobolectricSnackbarVerificationHelper : SnackbarVerificationHelper, TestRule by ShadowSnackbarResetTestRule() {

    override fun assertIsShownWithText(@StringRes stringResID: Int) {
        val latestSnackbar = ShadowSnackbar.latestSnackbar ?: throw IllegalStateException("Snackbar not found")
        Assert.assertEquals(latestSnackbar.context.getString(stringResID), ShadowSnackbar.textOfLatestSnackbar)
    }

    override fun assertIsNotShown() {
        Assert.assertNull(ShadowSnackbar.latestSnackbar)
    }
}
