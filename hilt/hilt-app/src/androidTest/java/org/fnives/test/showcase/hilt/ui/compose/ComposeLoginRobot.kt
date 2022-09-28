package org.fnives.test.showcase.hilt.ui.compose

import android.content.Context
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.fnives.test.showcase.hilt.compose.screen.auth.AuthScreenTag

class ComposeLoginRobot(
    semanticsNodeInteractionsProvider: SemanticsNodeInteractionsProvider,
) : SemanticsNodeInteractionsProvider by semanticsNodeInteractionsProvider {

    fun setUsername(username: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.UsernameInput).performTextInput(username)
    }

    fun setPassword(password: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.PasswordInput).performTextInput(password)
    }

    fun assertPassword(password: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.PasswordVisibilityToggle).performClick()
        onNodeWithTag(AuthScreenTag.PasswordInput).assertTextContains(password)
    }

    fun assertUsername(username: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.UsernameInput).assertTextContains(username)
    }

    fun clickOnLogin(): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.LoginButton).performClick()
    }

    fun assertLoading(): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.LoadingIndicator).assertIsDisplayed()
    }

    fun assertNotLoading(): ComposeLoginRobot = apply {
        onAllNodesWithTag(AuthScreenTag.LoadingIndicator).assertCountEquals(0)
    }

    fun assertErrorIsShown(stringId: Int): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.LoginError)
            .assertTextContains(ApplicationProvider.getApplicationContext<Context>().resources.getString(stringId))
    }
}
